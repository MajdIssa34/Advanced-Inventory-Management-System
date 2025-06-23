package com.codewithmajd.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name cannot be blank")
        String name,

        String description, // Description is optional

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be zero or positive")
        BigDecimal price,

        @NotBlank(message = "SKU Code cannot be blank")
        String skuCode
) {
}