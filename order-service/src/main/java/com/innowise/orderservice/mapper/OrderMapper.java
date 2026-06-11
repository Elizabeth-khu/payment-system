package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderResponse;
import com.innowise.orderservice.dto.UserInfoResponse;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "items", source = "order.orderItems")
    @Mapping(target = "user", source = "userInfo") // <-- Берем юзера из второго параметра!
    OrderResponse toResponse(Order order, UserInfoResponse userInfo);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "price", source = "item.price")
    OrderResponse.OrderItemDto toItemDto(OrderItem orderItem);
}