package com.codewithmajd.order_service;

import com.codewithmajd.order_service.repo.OrderRepo;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance().build();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderRepo orderRepo;

    // The KafkaTemplate MockitoBean has been removed.

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.cloud.loadbalancer.clients.inventory-service.instances[0].uri", wireMockServer::baseUrl);
    }

    @BeforeEach
    void setup() {
        orderRepo.deleteAll();
    }

    @Test
    void shouldPlaceOrderSuccessfully() throws Exception {
        // ARRANGE
        String tenantId = "tenant-electronics-store";
        setupWiremockSuccessStubs();
        String orderRequestJson = getValidOrderRequestJson();

        // ACTION & ASSERT
        mockMvc.perform(post("/api/order")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson))
                .andExpect(status().isCreated());

        assertEquals(1, orderRepo.count());
        var order = orderRepo.findAll().get(0);
        assertEquals(tenantId, order.getTenantId());

        // Kafka verification has been removed.
    }

    @Test
    void shouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        // ARRANGE: Invalid quantity (0 is not @Positive)
        String tenantId = "tenant-electronics-store";
        String invalidOrderRequestJson = """
            {
                "orderLineItemsDtoList": [
                    { "skuCode": "iphone_15", "price": 1500, "quantity": 0 }
                ]
            }
            """;

        // ACTION & ASSERT
        mockMvc.perform(post("/api/order")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidOrderRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.validationErrors['orderLineItemsDtoList[0].quantity']", is("Quantity must be at least 1")));

        assertEquals(0, orderRepo.count());
    }

    @Test
    void shouldReturnBadRequest_WhenInventoryIsInsufficient() throws Exception {
        // ARRANGE
        String tenantId = "tenant-electronics-store";
        String orderRequestJson = getValidOrderRequestJson();

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/inventory"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        [
                            {"skuCode": "iphone_15", "isInStock": true, "quantity": 0}
                        ]
                        """)));

        // ACTION & ASSERT
        mockMvc.perform(post("/api/order")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Not enough stock for SKU: iphone_15. Requested: 1, Available: 0")));

        assertEquals(0, orderRepo.count());
    }

    @Test
    void shouldReturnError_WhenInventoryServiceIsDown() throws Exception {
        // ARRANGE
        String tenantId = "tenant-electronics-store";
        String orderRequestJson = getValidOrderRequestJson();

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/inventory"))
                .willReturn(WireMock.aResponse().withStatus(503)));

        // ACTION & ASSERT
        mockMvc.perform(post("/api/order")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Inventory service is currently unavailable. Please try again later.")));

        assertEquals(0, orderRepo.count());
    }

    private void setupWiremockSuccessStubs() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/inventory"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        [
                            {"skuCode": "iphone_15", "isInStock": true, "quantity": 100}
                        ]
                        """)));

        wireMockServer.stubFor(WireMock.put(WireMock.urlPathEqualTo("/api/inventory/reduce-stock"))
                .willReturn(WireMock.aResponse().withStatus(200)));
    }

    private String getValidOrderRequestJson() {
        return """
            {
                "orderLineItemsDtoList": [
                    {
                        "skuCode": "iphone_15",
                        "price": 1500,
                        "quantity": 1
                    }
                ]
            }
            """;
    }
}