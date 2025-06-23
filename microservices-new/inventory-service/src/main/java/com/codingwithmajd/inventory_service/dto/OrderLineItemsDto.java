package com.codingwithmajd.inventory_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderLineItemsDto(
        // The 'id' field from the OrderLineItem entity is not needed here
        // for inventory management purposes. Price is also not needed.

        @NotBlank(message = "SKU Code cannot be blank")
        String skuCode,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity to reduce must be positive")
        Integer quantity
) {}