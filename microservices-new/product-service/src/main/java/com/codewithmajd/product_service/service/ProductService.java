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
        Product product = productRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setSkuCode(request.skuCode());
        productRepo.save(product);
        log.info("Product {} updated for tenant {}", product.getId(), tenantId);
    }

    public void deleteProduct(String id, String tenantId) {
        Product product = productRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id + " for this tenant"));

        productRepo.delete(product);
        log.info("Product {} deleted for tenant {}", id, tenantId);
    }
}