package com.codewithmajd.order_service.service;

import com.codewithmajd.order_service.dto.InventoryResponse;
import com.codewithmajd.order_service.dto.OrderLineItemsDto;
import com.codewithmajd.order_service.dto.OrderRequest;
import com.codewithmajd.order_service.model.Order;
import com.codewithmajd.order_service.model.OrderLineItems;
import com.codewithmajd.order_service.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final WebClient webClient;
    private final OrderRepo orderRepo;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto).toList();
                
        order.setOrderLineItemsList(orderLineItemsList);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode)
                .toList();

        InventoryResponse[] result = webClient.get()
                .uri("http://localhost:8083/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean finalRes = Arrays.stream(result).allMatch(InventoryResponse::isInStock);
        if(finalRes){
            orderRepo.save(order);
        }
        else{
            throw new IllegalArgumentException("Product not in stock :(");
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
