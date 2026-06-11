package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.OrderCreateRequest;
import com.innowise.orderservice.dto.OrderResponse;
import com.innowise.orderservice.dto.OrderUpdateRequest;
import com.innowise.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderCreateRequest request);
    OrderResponse getOrderById(String id);
    List<OrderResponse> getOrdersByUserId(String userId);
    Page<OrderResponse> getOrders(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Pageable pageable);
    OrderResponse updateOrderStatus(String id, OrderUpdateRequest request);
    void deleteOrderById(String id);
}
