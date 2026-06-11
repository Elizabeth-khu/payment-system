package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.dto.OrderCreateRequest;
import com.innowise.orderservice.dto.OrderItemRequest;
import com.innowise.orderservice.dto.OrderResponse;
import com.innowise.orderservice.dto.OrderUpdateRequest;
import com.innowise.orderservice.dto.UserInfoResponse;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.repository.OrderSpecification;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserServiceClient userServiceClient;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Order order = Order.builder()
                .userId(request.userId())
                .status(OrderStatus.CREATED)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for(OrderItemRequest itemReq : request.items()) {
            Item item = itemRepository.findById(itemReq.itemId())
                    .orElseThrow(() -> new ItemNotFoundException(itemReq.itemId()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .quantity(itemReq.quantity())
                    .build();

            orderItems.add(orderItem);
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalPrice = totalPrice.add(itemTotal);
        }
        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        UserInfoResponse user = getUserInfo(order.getUserId());
        return orderMapper.toResponse(savedOrder, user);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        UserInfoResponse user = getUserInfo(order.getUserId());
        return orderMapper.toResponse(order,user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Pageable pageable) {
        Specification<Order> spec = OrderSpecification.filterBy(from, to, statuses);
        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);

        return ordersPage.map(order -> {
            UserInfoResponse user = getUserInfo(order.getUserId());
            return orderMapper.toResponse(order, user);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(order -> {
                    UserInfoResponse user = getUserInfo(order.getUserId());
                    return orderMapper.toResponse(order,user);
                })
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setStatus(request.status());
        UserInfoResponse user = getUserInfo(order.getUserId());
        return orderMapper.toResponse(order, user);
    }

    @Override
    @Transactional
    public void deleteOrderById(String id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }
        orderRepository.deleteById(id);
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "userService", fallbackMethod = "fallbackUserInfo")
    private UserInfoResponse getUserInfo(String email) {
        return userServiceClient.getUserByEmail(email);
    }

    private UserInfoResponse fallbackUserInfo(String email, Throwable t) {
        return new UserInfoResponse("unknown", "System", "Unavailable", email);
    }
}
