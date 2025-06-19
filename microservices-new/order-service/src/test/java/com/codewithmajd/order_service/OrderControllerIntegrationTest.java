package com.codewithmajd.order_service;

import com.codewithmajd.order_service.event.OrderPlacedEvent;
import com.codewithmajd.order_service.repo.OrderRepo;
import com.codewithmajd.order_service.service.OrderService;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// NO @Import annotation needed here anymore
@SpringBootTest(properties = {
        "spring.cloud.loadbalancer.enabled=false",
        "eureka.client.enabled=false"
})
@Testcontainers
@AutoConfigureMockMvc
@ExtendWith(WireMockExtension.class)
public class OrderControllerIntegrationTest {


    // ... (@Container, @RegisterExtension, @Autowired fields are the same)
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance().build();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderRepo orderRepo;

    @MockitoBean
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    // This is the simplified, final version of this method
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("inventory.service.url", wireMockServer::baseUrl);


        // ✅ Explicitly enable LoadBalancer (make sure it's not disabled in any property file!)
        registry.add("spring.cloud.loadbalancer.enabled", () -> "true");

        // ✅ Tell WebClient's LoadBalancer to resolve inventory-service to WireMock
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

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/inventory"))
                .withQueryParam("skuCode", WireMock.containing("iphone_15"))  // not equalTo()
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        [
                            {"skuCode": "iphone_15", "isInStock": true, "quantity": 100}
                        ]
                        """)));

        wireMockServer.stubFor(WireMock.put("/api/inventory/reduce-stock")
                .willReturn(WireMock.aResponse().withStatus(200)));

        String orderRequestJson = """
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

        // ACTION
        mockMvc.perform(post("/api/order")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson))
                .andExpect(status().isCreated());

        // ASSERT
        assertEquals(1, orderRepo.count());
        var order = orderRepo.findAll().get(0);
        assertEquals(tenantId, order.getTenantId());

        ArgumentCaptor<OrderPlacedEvent> eventArgumentCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(kafkaTemplate, times(1)).send("notificationTopic", eventArgumentCaptor.capture());

        OrderPlacedEvent capturedEvent = eventArgumentCaptor.getValue();
        assertEquals(order.getOrderNumber(), capturedEvent.getOrderNumber());
        assertEquals(tenantId, capturedEvent.getTenantId());
    }
}