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
    public String placeOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("X-Tenant-ID") String tenantId){
        orderService.placeOrder(orderRequest, tenantId);
        return "Order Placed";
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getAllOrders(@RequestHeader("X-Tenant-ID") String tenantId) {
        return orderService.getAllOrders(tenantId);
    }

    @GetMapping("/{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    public Order getOrderByOrderNumber(@PathVariable String orderNumber, @RequestHeader("X-Tenant-ID") String tenantId) {
        return orderService.getOrderByOrderNumber(orderNumber, tenantId);
    }

    @GetMapping("/sku/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getOrdersBySkuCode(@PathVariable String skuCode, @RequestHeader("X-Tenant-ID") String tenantId) {
        return orderService.getOrdersBySkuCode(skuCode, tenantId);
    }

    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getRecentOrders(@RequestParam(defaultValue = "5") int limit, @RequestHeader("X-Tenant-ID") String tenantId) {
        return orderService.getRecentOrders(limit, tenantId);
    }
}
