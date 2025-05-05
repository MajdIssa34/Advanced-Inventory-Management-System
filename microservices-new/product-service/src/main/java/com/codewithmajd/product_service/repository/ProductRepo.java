package com.codewithmajd.product_service.repository;

import com.codewithmajd.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepo extends MongoRepository<Product,String> {
    Optional<Product> findBySkuCode(String skuCode);

}
