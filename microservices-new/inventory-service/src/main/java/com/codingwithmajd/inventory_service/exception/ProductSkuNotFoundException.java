package com.codingwithmajd.inventory_service.exception;

public class ProductSkuNotFoundException extends RuntimeException {
    public ProductSkuNotFoundException(String message) {
        super(message);
    }
}