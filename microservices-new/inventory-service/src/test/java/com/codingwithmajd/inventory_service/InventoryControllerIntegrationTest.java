package com.codingwithmajd.inventory_service;

import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class InventoryControllerIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

	@Autowired
	private MockMvc mockMvc;

	// THIS IS THE CRITICAL METHOD THAT FIXES THE ERROR
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
	}

	@Test
	void shouldCreateAndRetrieveInventoryForTenant() throws Exception {
		// ARRANGE
		String tenantA = "tenant-coffee-shop";
		String tenantB = "tenant-book-store";
		String inventoryRequestJson = """
            {
                "skuCode": "coffee-beans-001",
                "quantity": 100
            }
            """;

		// ACTION & ASSERT: Create inventory for Tenant A
		mockMvc.perform(post("/api/inventory")
						.header("X-Tenant-ID", tenantA)
						.contentType(MediaType.APPLICATION_JSON)
						.content(inventoryRequestJson))
				.andExpect(status().isCreated());

		// ACTION & ASSERT: Verify Tenant A can see their inventory
		mockMvc.perform(get("/api/inventory/all")
						.header("X-Tenant-ID", tenantA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].skuCode", is("coffee-beans-001")));

		// ACTION & ASSERT: Verify Tenant B has no inventory
		mockMvc.perform(get("/api/inventory/all")
						.header("X-Tenant-ID", tenantB))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}
}