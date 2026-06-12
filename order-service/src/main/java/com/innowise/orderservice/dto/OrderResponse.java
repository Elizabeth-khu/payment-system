package com.innowise.orderservice.dto;

import com.innowise.orderservice.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        UserInfoResponse user,
        OrderStatus status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        List<OrderItemDto> items
) {
    public record OrderItemDto(
            String itemId,
            String itemName,
            BigDecimal price,
            Integer quantity
    ) {}
}