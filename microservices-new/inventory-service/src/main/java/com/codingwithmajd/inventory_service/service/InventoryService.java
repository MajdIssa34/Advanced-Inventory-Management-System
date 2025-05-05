package com.codingwithmajd.inventory_service.service;

import com.codingwithmajd.inventory_service.Repo.InventoryRepo;
import com.codingwithmajd.inventory_service.dto.InventoryRequest;
import com.codingwithmajd.inventory_service.dto.InventoryResponse;
import com.codingwithmajd.inventory_service.dto.OrderLineItemsDto;
import com.codingwithmajd.inventory_service.model.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepo inventoryRepo;

    public void deleteInventory(String skuCode){
        Inventory inventory = inventoryRepo.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("SKU not found: " + skuCode));
        inventoryRepo.delete(inventory);
    }

    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        return inventoryRepo.findBySkuCodeIn(skuCodes).stream()
                .map(inventory -> new InventoryResponse(
                        inventory.getSkuCode(),
                        inventory.getQuantity() > 0,
                        inventory.getQuantity()
                ))
                .toList();
    }

    public void reduceStock(List<OrderLineItemsDto> items) {
        for (OrderLineItemsDto item : items) {
            Inventory inventory = inventoryRepo.findBySkuCode(item.getSkuCode())
                    .orElseThrow(() -> new RuntimeException("SKU not found: " + item.getSkuCode()));

            if (inventory.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for SKU: " + item.getSkuCode());
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepo.save(inventory);
        }
    }

    public void createInventory(InventoryRequest inventoryRequest) {
        Inventory inventory = new Inventory();
        inventory.setSkuCode(inventoryRequest.getSkuCode());
        inventory.setQuantity(inventoryRequest.getQuantity());
        inventoryRepo.save(inventory);
    }

    public List<InventoryResponse> getAllInventory(){
        return inventoryRepo.findAll().stream()
                .map(inv -> new InventoryResponse(inv.getSkuCode(), inv.getQuantity() > 0, inv.getQuantity()))
                .toList();
    }

    public void restock(InventoryRequest request) {
        Inventory inventory = inventoryRepo.findBySkuCode(request.getSkuCode())
                .orElseThrow(() -> new RuntimeException("SKU not found: " + request.getSkuCode()));
        inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        inventoryRepo.save(inventory);
    }

    public List<InventoryResponse> getLowStockItems(int threshold) {
        return inventoryRepo.findAll().stream()
                .filter(inv -> inv.getQuantity() <= threshold)
                .map(inv -> new InventoryResponse(inv.getSkuCode(), inv.getQuantity() > 0, inv.getQuantity()))
                .toList();
    }

}
