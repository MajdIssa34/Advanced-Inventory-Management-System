package com.codewithmajd.order_service.service;

import com.codewithmajd.order_service.dto.InventoryResponse;
import com.codewithmajd.order_service.dto.OrderLineItemsDto;
import com.codewithmajd.order_service.dto.OrderRequest;
import com.codewithmajd.order_service.exception.OrderNotFoundException;
import com.codewithmajd.order_service.exception.OrderPlacementException;
import com.codewithmajd.order_service.model.Order;
import com.codewithmajd.order_service.model.OrderLineItems;
import com.codewithmajd.order_service.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final WebClient.Builder webClientBuilder;
    private final OrderRepo orderRepo;
    // Removed KafkaTemplate for simplicity in this example, can be added back if needed

    public void placeOrder(OrderRequest orderRequest, String tenantId) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setTenantId(tenantId);

        List<OrderLineItems> orderLineItems = orderRequest.orderLineItemsDtoList()
                .stream()
                .map(this::mapToEntity)
                .toList();
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = orderLineItems.stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // Call Inventory Service to check stock
        InventoryResponse[] inventoryResponses = checkInventory(skuCodes, tenantId);

        // Create a map for easy lookup of inventory data
        Map<String, InventoryResponse> inventoryMap = Arrays.stream(inventoryResponses)
                .collect(Collectors.toMap(InventoryResponse::getSkuCode, Function.identity()));

        // Validate that all products exist and have sufficient stock
        validateStock(orderLineItems, inventoryMap);

        orderRepo.save(order);

        // After saving, tell Inventory Service to reduce stock
        reduceStock(orderLineItems, tenantId);
    }

    private InventoryResponse[] checkInventory(List<String> skuCodes, String tenantId) {
        // Assuming inventory-service is registered with Eureka as "inventory-service"
        return webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .header("X-Tenant-ID", tenantId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new OrderPlacementException("One or more products could not be found in inventory.")))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new OrderPlacementException("Inventory service is currently unavailable. Please try again later.")))
                .bodyToMono(InventoryResponse[].class)
                .block();
    }

    private void validateStock(List<OrderLineItems> orderLineItems, Map<String, InventoryResponse> inventoryMap) {
        for (OrderLineItems item : orderLineItems) {
            InventoryResponse inventoryData = inventoryMap.get(item.getSkuCode());

            if (inventoryData == null) {
                throw new OrderPlacementException("Product with SKU " + item.getSkuCode() + " does not exist in inventory.");
            }
            if (!inventoryData.isInStock() || inventoryData.getQuantity() < item.getQuantity()) {
                throw new OrderPlacementException("Not enough stock for product with SKU: " + item.getSkuCode()
                        + ". Requested: " + item.getQuantity() + ", Available: " + inventoryData.getQuantity());
            }
        }
    }

    private void reduceStock(List<OrderLineItems> orderLineItems, String tenantId) {
        List<OrderLineItemsDto> itemsToReduce = orderLineItems.stream()
                .map(item -> new OrderLineItemsDto(item.getSkuCode(), item.getPrice(), item.getQuantity()))
                .toList();

        webClientBuilder.build().put()
                .uri("http://inventory-service/api/inventory/reduce-stock")
                .header("X-Tenant-ID", tenantId)
                .bodyValue(itemsToReduce)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new OrderPlacementException("Failed to update inventory stock. Order has been rolled back.")))
                .bodyToMono(Void.class)
                .block();
    }

    public List<Order> getAllOrders(String tenantId) {
        return orderRepo.findByTenantId(tenantId);
    }

    public Order getOrderByOrderNumber(String orderNumber, String tenantId) {
        return orderRepo.findByOrderNumberAndTenantId(orderNumber, tenantId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with order number: " + orderNumber));
    }

    public List<Order> getOrdersBySkuCode(String skuCode, String tenantId) {
        return orderRepo.findByLineItemSkuCodeAndTenantId(skuCode, tenantId);
    }

    public List<Order> getRecentOrders(int limit, String tenantId) {
        return orderRepo.findByTenantId(tenantId).stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId())) // newest first
                .limit(limit)
                .toList();
    }

    private OrderLineItems mapToEntity(OrderLineItemsDto dto) {
        OrderLineItems item = new OrderLineItems();
        item.setPrice(dto.price());
        item.setQuantity(dto.quantity());
        item.setSkuCode(dto.skuCode());
        return item;
    }
}