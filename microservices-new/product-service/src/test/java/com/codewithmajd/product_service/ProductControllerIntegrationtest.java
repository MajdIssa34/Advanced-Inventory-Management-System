package com.codewithmajd.product_service;

import com.codewithmajd.product_service.dto.ProductRequest;
import com.codewithmajd.product_service.repository.ProductRepo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// Imports for Spring MockMvc static methods
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Imports for Hamcrest matchers (for 'is' and 'hasSize')
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void shouldCreateAndRetrieveProductForTenant() throws Exception {
        String tenant1 = "tenant-A";
        String productRequestJson = """
            {
                "name": "Test Product",
                "description": "A product for Tenant A",
                "price": 10.99,
                "skuCode": "SKU123"
            }
            """;

        // Create product for Tenant A
        mockMvc.perform(post("/api/product")
                        .header("X-Tenant-ID", tenant1) // Simulate the header from the Gateway
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestJson))
                .andExpect(status().isCreated());

        // Verify we can get it back for Tenant A
        mockMvc.perform(get("/api/product")
                        .header("X-Tenant-ID", tenant1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Product")));

        // Verify that Tenant B has no products
        mockMvc.perform(get("/api/product")
                        .header("X-Tenant-ID", "tenant-B")) // Use a different tenant ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Expect an empty array
    }
}
