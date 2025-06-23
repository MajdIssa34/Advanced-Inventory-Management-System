package com.codewithmajd.order_service.exception;

public class OrderPlacementException extends RuntimeException {
    public OrderPlacementException(String message) {
        super(message);
    }
}