package com.codingwithmajd.inventory_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryRequest(
        @NotBlank(message = "SKU Code cannot be blank")
        String skuCode,

        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be zero or positive")
        Integer quantity
) {}