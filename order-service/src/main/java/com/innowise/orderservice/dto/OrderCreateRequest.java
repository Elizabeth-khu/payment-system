package com.innowise.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderCreateRequest(
        @NotBlank(message = "User ID is mandatory")
        String userId,
        @NotBlank(message = "Idempotency key is mandatory")
        String idempotencyKey,
        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid OrderItemRequest> items
) {}