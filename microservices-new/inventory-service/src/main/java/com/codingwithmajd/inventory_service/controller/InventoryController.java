package com.codingwithmajd.inventory_service.controller;

import com.codingwithmajd.inventory_service.dto.InventoryRequest;
import com.codingwithmajd.inventory_service.dto.InventoryResponse;
import com.codingwithmajd.inventory_service.dto.OrderLineItemsDto;
import com.codingwithmajd.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode, @RequestHeader("X-Tenant-ID") String tenantId){
        return inventoryService.isInStock(skuCode, tenantId);
    }

    @PutMapping("/reduce-stock")
    @ResponseStatus(HttpStatus.OK)
    public void reduceStock(@RequestBody List<OrderLineItemsDto> items, @RequestHeader("X-Tenant-ID") String tenantId) {
        inventoryService.reduceStock(items, tenantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createInventory(@RequestBody InventoryRequest inventoryRequest, @RequestHeader("X-Tenant-ID") String tenantId) {
        inventoryService.createInventory(inventoryRequest, tenantId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteInventory(@RequestParam String skuCode, @RequestHeader("X-Tenant-ID") String tenantId){
        inventoryService.deleteInventory(skuCode, tenantId);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getAllInventory(@RequestHeader("X-Tenant-ID") String tenantId){
        return inventoryService.getAllInventory(tenantId);
    }

    @PutMapping("/restock")
    @ResponseStatus(HttpStatus.OK)
    public void restockInventory(@RequestBody InventoryRequest inventoryRequest, @RequestHeader("X-Tenant-ID") String tenantId) {
        inventoryService.restock(inventoryRequest, tenantId);
    }

    @GetMapping("/low-stock")
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getLowStockItems(@RequestParam(defaultValue = "10") Integer threshold, @RequestHeader("X-Tenant-ID") String tenantId) {
        return inventoryService.getLowStockItems(threshold, tenantId);
    }

}
