package com.codewithmajd.product_service.service;

import com.codewithmajd.product_service.dto.ProductRequest;
import com.codewithmajd.product_service.dto.ProductResponse;
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

        boolean exists = productRepo.findBySkuCodeAndTenantId(productRequest.getSkuCode(), tenantId).isPresent();
        if (exists) {
            throw new RuntimeException("SKU code already exists: " + productRequest.getSkuCode());
        }

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .skuCode(productRequest.getSkuCode())
                .tenantId(tenantId)
                .build();

        productRepo.save(product);
        log.info("Product {} created", product.getSkuCode());
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
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    public ProductResponse getProductBySku(String skuCode, String tenantId) {
        Product product = productRepo.findBySkuCodeAndTenantId(skuCode, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found with sku: " + skuCode));
        return mapToProductResponse(product);
    }

    public void updateProduct(String id, ProductRequest request, String tenantId) {
        Product product = productRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSkuCode(request.getSkuCode());
        productRepo.save(product);
        log.info("Product {} updated for tenant {}", product.getId(), tenantId);
    }

    public void deleteProduct(String id, String tenantId) {
        boolean exists = productRepo.findByIdAndTenantId(id, tenantId).isPresent();
        if (!exists) {
            throw new RuntimeException("Product not found with id: " + id + " for this tenant");
        }
        productRepo.deleteByIdAndTenantId(id, tenantId);
        log.info("Product {} deleted for tenant {}", id, tenantId);
    }

}
