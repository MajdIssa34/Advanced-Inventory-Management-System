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

    public void createProduct(ProductRequest productRequest){

        boolean exists = productRepo.findBySkuCode(productRequest.getSkuCode()).isPresent();
        if (exists) {
            throw new RuntimeException("SKU code already exists: " + productRequest.getSkuCode());
        }

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .skuCode(productRequest.getSkuCode())
                .build();

        productRepo.save(product);
        log.info("Product {} created", product.getSkuCode());
    }

    public List<ProductResponse> getAllProducts(){
        List<Product> products = productRepo.findAll();

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

    public ProductResponse getProductById(String id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    public ProductResponse getProductBySku(String skuCode) {
        Product product = productRepo.findAll().stream()
                .filter(p -> skuCode.equals(p.getSkuCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found with sku: " + skuCode));
        return mapToProductResponse(product);
    }

    public void updateProduct(String id, ProductRequest request) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSkuCode(request.getSkuCode());
        productRepo.save(product);
    }

    public void deleteProduct(String id) {
        productRepo.deleteById(id);
    }

}
