package com.codingwithmajd.inventory_service.service;

import com.codingwithmajd.inventory_service.Repo.InventoryRepo;
import com.codingwithmajd.inventory_service.dto.InventoryRequest;
import com.codingwithmajd.inventory_service.dto.InventoryResponse;
import com.codingwithmajd.inventory_service.dto.OrderLineItemsDto;
import com.codingwithmajd.inventory_service.model.Inventory;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepo inventoryRepo;

    public void deleteInventory(String skuCode, String tenantId){
        // First, check if the item exists for this tenant to provide a good error message
        inventoryRepo.findBySkuCodeAndTenantId(skuCode, tenantId)
                .orElseThrow(() -> new RuntimeException("SKU not found: " + skuCode + " for this tenant"));

        // Use the new tenant-aware delete method
        inventoryRepo.deleteBySkuCodeAndTenantId(skuCode, tenantId);
    }
    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes, String tenantId) {
        return inventoryRepo.findBySkuCodeInAndTenantId(skuCodes, tenantId).stream()
                .map(inventory -> new InventoryResponse(
                        inventory.getSkuCode(),
                        inventory.getQuantity() > 0,
                        inventory.getQuantity()
                ))
                .toList();
    }

    public void reduceStock(List<OrderLineItemsDto> items, String tenantId) {
        for (OrderLineItemsDto item : items) {
            Inventory inventory = inventoryRepo.findBySkuCodeAndTenantId(item.getSkuCode(), tenantId)
                    .orElseThrow(() -> new RuntimeException("SKU not found: " + item.getSkuCode()));

            if (inventory.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for SKU: " + item.getSkuCode());
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepo.save(inventory);
        }
    }

    public void createInventory(InventoryRequest inventoryRequest, String tenantId) {
        // Ensure the SKU doesn't already exist for this tenant
        inventoryRepo.findBySkuCodeAndTenantId(inventoryRequest.getSkuCode(), tenantId)
                .ifPresent(inv -> {
                    throw new IllegalArgumentException("SKU already exists for this tenant: " + inv.getSkuCode());
                });

        Inventory inventory = new Inventory();
        inventory.setSkuCode(inventoryRequest.getSkuCode());
        inventory.setQuantity(inventoryRequest.getQuantity());
        inventory.setTenantId(tenantId);
        inventoryRepo.save(inventory);
    }

    public List<InventoryResponse> getAllInventory(String tenantId){
        return inventoryRepo.findByTenantId(tenantId).stream()
                .map(inv -> new InventoryResponse(inv.getSkuCode(), inv.getQuantity() > 0, inv.getQuantity()))
                .toList();
    }

    public void restock(InventoryRequest request, String tenantId) {
        Inventory inventory = inventoryRepo.findBySkuCodeAndTenantId(request.getSkuCode(), tenantId)
                .orElseThrow(() -> new RuntimeException("SKU not found: " + request.getSkuCode() + " for this tenant"));
        inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        inventoryRepo.save(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems(int threshold, String tenantId) {
        return inventoryRepo.findByTenantId(tenantId).stream()
                .filter(inv -> inv.getQuantity() <= threshold)
                .map(inv -> new InventoryResponse(inv.getSkuCode(), inv.getQuantity() > 0, inv.getQuantity()))
                .toList();
    }

}
