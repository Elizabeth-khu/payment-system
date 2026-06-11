package com.innowise.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotBlank(message = "Item ID is mandatory")
        String itemId,

        @NotNull(message = "Quantity is mandatory")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {}