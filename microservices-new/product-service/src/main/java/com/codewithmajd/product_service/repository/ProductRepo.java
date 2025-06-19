package com.codewithmajd.product_service.repository;

import com.codewithmajd.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends MongoRepository<Product,String> {
    Optional<Product> findBySkuCode(String skuCode);
    // Replace the old findBySkuCode with this
    Optional<Product> findBySkuCodeAndTenantId(String skuCode, String tenantId);

    // New method to get all products for a specific tenant
    List<Product> findByTenantId(String tenantId);

    // New method to find a specific product for a specific tenant
    Optional<Product> findByIdAndTenantId(String id, String tenantId);

    // New method to delete a product for a specific tenant
    void deleteByIdAndTenantId(String id, String tenantId);
}
