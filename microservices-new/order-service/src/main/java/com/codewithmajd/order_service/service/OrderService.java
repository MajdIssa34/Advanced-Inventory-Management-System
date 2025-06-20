package com.codewithmajd.order_service.service;

import com.codewithmajd.order_service.dto.InventoryResponse;
import com.codewithmajd.order_service.dto.OrderLineItemsDto;
import com.codewithmajd.order_service.dto.OrderRequest;
import com.codewithmajd.order_service.event.OrderPlacedEvent;
import com.codewithmajd.order_service.model.Order;
import com.codewithmajd.order_service.model.OrderLineItems;
import com.codewithmajd.order_service.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final WebClient.Builder webClientBuilder;
    private final OrderRepo orderRepo;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final String inventoryServiceUrl;

    public OrderService(WebClient.Builder webClientBuilder,
                        OrderRepo orderRepo,
                        KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
                        @Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.webClientBuilder = webClientBuilder;
        this.orderRepo = orderRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }


    public void placeOrder(OrderRequest orderRequest, String tenantId) {
        // ... (setting orderNumber, tenantId, and creating orderLineItemsList is the same)
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setTenantId(tenantId);

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        order.setOrderLineItemsList(orderLineItemsList);

        List<String> skuCodes = orderLineItemsList.stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // --- CHANGE 2: BUILD THE URI DYNAMICALLY ---
        String inventoryCheckUri = UriComponentsBuilder.fromHttpUrl(inventoryServiceUrl)
                .path("/api/inventory")
                .queryParam("skuCode", skuCodes)
                .build().toUriString();

        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri(inventoryCheckUri) // Use the dynamically built URI
                .header("X-Tenant-ID", tenantId)
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        // ... (The rest of the validation logic is the same)
        // Validate SKU existence
        List<String> foundSkus = Arrays.stream(inventoryResponses)
                .map(InventoryResponse::getSkuCode)
                .toList();
        for (String sku : skuCodes) {
            if (!foundSkus.contains(sku)) {
                throw new IllegalArgumentException("SKU not found in inventory: " + sku);
            }
        }
        // Validate quantity
        boolean allInStock = orderLineItemsList.stream().allMatch(orderItem ->
                Arrays.stream(inventoryResponses).anyMatch(inventoryItem ->
                        inventoryItem.getSkuCode().equals(orderItem.getSkuCode()) &&
                                inventoryItem.isInStock() &&
                                inventoryItem.getQuantity() >= orderItem.getQuantity()
                )
        );

        if (allInStock) {
            orderRepo.save(order);

            // --- CHANGE 3: USE THE CONFIGURED URL FOR REDUCE-STOCK ---
            String reduceStockUri = UriComponentsBuilder.fromHttpUrl(inventoryServiceUrl)
                    .path("/api/inventory/reduce-stock")
                    .build().toUriString();

            webClientBuilder.build().put()
                    .uri(reduceStockUri)
                    .header("X-Tenant-ID", tenantId)
                    .bodyValue(orderLineItemsList)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } else {
            throw new IllegalArgumentException("One or more products are out of stock or quantity is insufficient.");
        }
    }

    public List<Order> getAllOrders(String tenantId) {
        return orderRepo.findByTenantId(tenantId);
    }

    public Order getOrderByOrderNumber(String orderNumber, String tenantId) {
        return orderRepo.findByOrderNumberAndTenantId(orderNumber, tenantId)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    public List<Order> getOrdersBySkuCode(String skuCode, String tenantId) {
        return orderRepo.findByLineItemSkuCodeAndTenantId(skuCode, tenantId);
    }

    public List<Order> getRecentOrders(int limit, String tenantId) {
        return orderRepo.findByTenantId(tenantId).stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId())) // newest first
                .limit(limit)
                .toList();
    }

    private OrderLineItems mapToDto(OrderLineItemsDto dto) {
        OrderLineItems item = new OrderLineItems();
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setSkuCode(dto.getSkuCode());
        return item;
    }
}
