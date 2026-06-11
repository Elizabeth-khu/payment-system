package com.innowise.orderservice.dto;

import com.innowise.orderservice.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderUpdateRequest (
        @NotNull(message = "Status cannot be null")
        OrderStatus status
){}
