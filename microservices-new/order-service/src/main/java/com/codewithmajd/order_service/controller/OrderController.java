package com.codewithmajd.order_service.controller;

import com.codewithmajd.order_service.dto.OrderRequest;
import com.codewithmajd.order_service.model.Order;
import com.codewithmajd.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(code = org.springframework.http.HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest){
        orderService.placeOrder(orderRequest);
        return "Order Placed";
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    public Order getOrderByOrderNumber(@PathVariable String orderNumber) {
        return orderService.getOrderByOrderNumber(orderNumber);
    }

    @GetMapping("/sku/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getOrdersBySkuCode(@PathVariable String skuCode) {
        return orderService.getOrdersBySkuCode(skuCode);
    }

    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getRecentOrders(@RequestParam(defaultValue = "5") int limit) {
        return orderService.getRecentOrders(limit);
    }
}
