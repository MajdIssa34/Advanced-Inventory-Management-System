package com.codewithmajd.product_service.controller;

import com.codewithmajd.product_service.dto.ProductRequest;
import com.codewithmajd.product_service.dto.ProductResponse;
import com.codewithmajd.product_service.model.Product;
import com.codewithmajd.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createProduct(@RequestBody ProductRequest productRequest, @RequestHeader("X-Tenant-ID") String tenantId){
        productService.createProduct(productRequest, tenantId);
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<ProductResponse> getAllProducts(@RequestHeader("X-Tenant-ID") String tenantId){
        return productService.getAllProducts(tenantId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(@PathVariable String id, @RequestHeader("X-Tenant-ID") String tenantId) {
        return productService.getProductById(id, tenantId);
    }

    @GetMapping("/sku/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductBySku(@PathVariable String skuCode, @RequestHeader("X-Tenant-ID") String tenantId) {
        return productService.getProductBySku(skuCode, tenantId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateProduct(@PathVariable String id, @RequestBody ProductRequest request, @RequestHeader("X-Tenant-ID") String tenantId) {
        productService.updateProduct(id, request, tenantId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id, @RequestHeader("X-Tenant-ID") String tenantId) {
        productService.deleteProduct(id, tenantId);
    }

}
