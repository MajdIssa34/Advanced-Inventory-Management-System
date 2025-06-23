package com.codewithmajd.product_service.service;

import com.codewithmajd.product_service.dto.ProductRequest;
import com.codewithmajd.product_service.dto.ProductResponse;
import com.codewithmajd.product_service.exception.ProductAlreadyExistsException;
import com.codewithmajd.product_service.exception.ProductNotFoundException;
import com.codewithmajd.product_service.model.Product;
import com.codewithmajd.product_service.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepo productRepo;

    public void createProduct(ProductRequest productRequest, String tenantId){
        boolean exists = productRepo.findBySkuCodeAndTenantId(productRequest.skuCode(), tenantId).isPresent();
        if (exists) {
            throw new ProductAlreadyExistsException("SKU code already exists: " + productRequest.skuCode());
        }

        Product product = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .skuCode(productRequest.skuCode())
                .tenantId(tenantId)
                .build();

        productRepo.save(product);
        log.info("Product {} created for tenant {}", product.getSkuCode(), tenantId);
    }

    public List<ProductResponse> getAllProducts(String tenantId){
        List<Product> products = productRepo.findByTenantId(tenantId);
        return products.stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .skuCode(product.getSkuCode())
                .build();
    }

    public ProductResponse getProductById(String id, String tenantId) {
        Product product = productRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    public ProductResponse getProductBySku(String skuCode, String tenantId) {
        Product product = productRepo.findBySkuCodeAndTenantId(skuCode, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with sku: " + skuCode));
        return mapToProductResponse(product);
    }

    public void updateProduct(String id, ProductRequest request, String tenantId) {
        // 1. Find the product we intend to update
        Product productToUpdate = productRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        // 2. Check if the new SKU is already taken by ANOTHER product
        productRepo.findBySkuCodeAndTenantId(request.skuCode(), tenantId)
                .ifPresent(existingProduct -> {
                    // If a product with the new SKU exists, we must check if it's a different product.
                    // It's okay to save if we are updating the product with its own existing SKU.
                    if (!existingProduct.getId().equals(productToUpdate.getId())) {
                        throw new ProductAlreadyExistsException("SKU code " + request.skuCode() + " is already in use by another product.");
                    }
                });

        // 3. If the check passes, update the product's fields and save
        productToUpdate.setName(request.name());
        productToUpdate.setDescription(request.description());
        productToUpdate.setPrice(request.price());
        productToUpdate.setSkuCode(request.skuCode());

        productRepo.save(productToUpdate);
        log.info("Product {} updated for tenant {}", productToUpdate.getId(), tenantId);
    }

    public void deleteProduct(String id, String tenantId) {
        Product product = productRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id + " for this tenant"));

        productRepo.delete(product);
        log.info("Product {} deleted for tenant {}", id, tenantId);
    }
}