package com.codewithmajd.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderLineItemsDto> orderLineItemsDtoList
) {}