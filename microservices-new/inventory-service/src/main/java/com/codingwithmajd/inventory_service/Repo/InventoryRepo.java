package com.codingwithmajd.inventory_service.Repo;

import com.codingwithmajd.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepo extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySkuCodeAndTenantId(String skuCode, String tenantId);

    List<Inventory> findBySkuCodeInAndTenantId(List<String> skuCodes, String tenantId);

    List<Inventory> findByTenantId(String tenantId);

    void deleteBySkuCodeAndTenantId(String skuCode, String tenantId);
}