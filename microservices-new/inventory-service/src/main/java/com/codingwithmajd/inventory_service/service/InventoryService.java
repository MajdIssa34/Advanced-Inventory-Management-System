package com.codingwithmajd.inventory_service.service;

import com.codingwithmajd.inventory_service.Repo.InventoryRepo;
import com.codingwithmajd.inventory_service.dto.InventoryRequest;
import com.codingwithmajd.inventory_service.dto.InventoryResponse;
import com.codingwithmajd.inventory_service.dto.OrderLineItemsDto;
import com.codingwithmajd.inventory_service.exception.InsufficientStockException;
import com.codingwithmajd.inventory_service.exception.InventoryAlreadyExistsException;
import com.codingwithmajd.inventory_service.exception.InventoryNotFoundException;
import com.codingwithmajd.inventory_service.model.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryService {

    private final InventoryRepo inventoryRepo;

    public void deleteInventory(String skuCode, String tenantId){
        inventoryRepo.findBySkuCodeAndTenantId(skuCode, tenantId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + skuCode));
        inventoryRepo.deleteBySkuCodeAndTenantId(skuCode, tenantId);
        log.info("Inventory deleted for SKU: {}, Tenant: {}", skuCode, tenantId);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes, String tenantId) {
        // This logic is fine, no exceptions thrown here typically
        return inventoryRepo.findBySkuCodeInAndTenantId(skuCodes, tenantId).stream()
                .map(inventory -> new InventoryResponse(inventory.getSkuCode(), inventory.getQuantity() > 0, inventory.getQuantity()))
                .toList();
    }

    public void reduceStock(List<OrderLineItemsDto> items, String tenantId) {
        for (OrderLineItemsDto item : items) {
            Inventory inventory = inventoryRepo.findBySkuCodeAndTenantId(item.skuCode(), tenantId)
                    .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + item.skuCode()));

            if (inventory.getQuantity() < item.quantity()) {
                throw new InsufficientStockException("Not enough stock for SKU: " + item.skuCode()
                        + ". Requested: " + item.quantity() + ", Available: " + inventory.getQuantity());
            }

            inventory.setQuantity(inventory.getQuantity() - item.quantity());
            inventoryRepo.save(inventory);
        }
        log.info("Stock reduced for tenant {}", tenantId);
    }

    public void createInventory(InventoryRequest inventoryRequest, String tenantId) {
        inventoryRepo.findBySkuCodeAndTenantId(inventoryRequest.skuCode(), tenantId)
                .ifPresent(inv -> {
                    throw new InventoryAlreadyExistsException("Inventory already exists for SKU: " + inv.getSkuCode());
                });

        Inventory inventory = new Inventory();
        inventory.setSkuCode(inventoryRequest.skuCode());
        inventory.setQuantity(inventoryRequest.quantity());
        inventory.setTenantId(tenantId);
        inventoryRepo.save(inventory);
        log.info("Inventory created for SKU: {}, Tenant: {}", inventory.getSkuCode(), tenantId);
    }

    public List<InventoryResponse> getAllInventory(String tenantId){
        return inventoryRepo.findByTenantId(tenantId).stream()
                .map(inv -> new InventoryResponse(inv.getSkuCode(), inv.getQuantity() > 0, inv.getQuantity()))
                .toList();
    }

    public void restock(InventoryRequest request, String tenantId) {
        Inventory inventory = inventoryRepo.findBySkuCodeAndTenantId(request.skuCode(), tenantId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + request.skuCode()));
        inventory.setQuantity(inventory.getQuantity() + request.quantity());
        inventoryRepo.save(inventory);
        log.info("Restocked SKU: {}. New quantity: {}", inventory.getSkuCode(), inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems(int threshold, String tenantId) {
        return inventoryRepo.findByTenantId(tenantId).stream()
                .filter(inv -> inv.getQuantity() <= threshold)
                .map(inv -> new InventoryResponse(inv.getSkuCode(), inv.getQuantity() > 0, inv.getQuantity()))
                .toList();
    }
}