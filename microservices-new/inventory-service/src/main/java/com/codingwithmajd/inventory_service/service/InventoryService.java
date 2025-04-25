package com.codingwithmajd.inventory_service.service;

import com.codingwithmajd.inventory_service.Repo.InventoryRepo;
import com.codingwithmajd.inventory_service.dto.InventoryResponse;
import com.codingwithmajd.inventory_service.model.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepo inventoryRepo;

    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        return inventoryRepo.findBySkuCodeIn(skuCodes).stream()
                .map(inventory -> new InventoryResponse(
                        inventory.getSkuCode(),
                        inventory.getQuantity() > 0,
                        inventory.getQuantity()
                ))
                .toList();
    }

    public void reduceStock(List<InventoryResponse> items) {
        for (InventoryResponse item : items) {
            Inventory inventory = inventoryRepo.findBySkuCode(item.getSkuCode())
                    .orElseThrow(() -> new RuntimeException("SKU not found: " + item.getSkuCode()));

            if (inventory.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for SKU: " + item.getSkuCode());
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepo.save(inventory);
        }
    }



}
