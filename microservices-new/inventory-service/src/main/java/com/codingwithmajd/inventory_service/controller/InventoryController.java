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
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode){
        return inventoryService.isInStock(skuCode);
    }

    @PutMapping("/reduce-stock")
    @ResponseStatus(HttpStatus.OK)
    public void reduceStock(@RequestBody List<OrderLineItemsDto> items) {
        inventoryService.reduceStock(items);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createInventory(@RequestBody InventoryRequest inventoryRequest) {
        inventoryService.createInventory(inventoryRequest);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteInventory(@RequestParam String skuCode){
        inventoryService.deleteInventory(skuCode);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getAllInventory(){
        return inventoryService.getAllInventory();
    }

    @PutMapping("/restock")
    @ResponseStatus(HttpStatus.OK)
    public void restockInventory(@RequestBody InventoryRequest inventoryRequest) {
        inventoryService.restock(inventoryRequest);
    }

    @GetMapping("/low-stock")
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getLowStockItems(@RequestParam(defaultValue = "10") Integer threshold) {
        return inventoryService.getLowStockItems(threshold);
    }

}
