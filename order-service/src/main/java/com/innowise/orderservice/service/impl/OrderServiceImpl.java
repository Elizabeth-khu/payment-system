package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceAdapter;
import com.innowise.orderservice.dto.OrderCreateRequest;
import com.innowise.orderservice.dto.OrderItemRequest;
import com.innowise.orderservice.dto.OrderResponse;
import com.innowise.orderservice.dto.OrderUpdateRequest;
import com.innowise.orderservice.dto.UserInfoResponse;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.exception.DuplicateOrderException;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.repository.OrderSpecification;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final UserServiceAdapter userServiceAdapter;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(String idempotencyKey, OrderCreateRequest request) {
        if (orderRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateOrderException("Order with this idempotency key already processing");
        }

        Order order = Order.builder()
                .userId(request.userId())
                .idempotencyKey(idempotencyKey)
                .status(OrderStatus.CREATED)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            Item item = itemRepository.findById(itemReq.itemId())
                    .orElseThrow(() -> new ItemNotFoundException(itemReq.itemId()));

            OrderItem orderItem = OrderItem.builder()
                    .item(item)
                    .quantity(itemReq.quantity())
                    .build();

            order.addOrderItem(orderItem);

            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalPrice = totalPrice.add(itemTotal);
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        UserInfoResponse user = userServiceAdapter.getUserInfo(order.getUserId());
        return orderMapper.toResponse(savedOrder, user);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        UserInfoResponse user = userServiceAdapter.getUserInfo(order.getUserId());
        return orderMapper.toResponse(order, user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(String userId, LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Pageable pageable) {
        Specification<Order> spec = OrderSpecification.filterBy(userId, from, to, statuses);
        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);

        List<String> distinctUserIds = ordersPage.getContent().stream()
                .map(Order::getUserId)
                .distinct()
                .toList();

        Map<String, UserInfoResponse> usersCache = new HashMap<>();
        for (String uid : distinctUserIds) {
            usersCache.put(uid, userServiceAdapter.getUserInfo(uid));
        }

        return ordersPage.map(order -> {
            UserInfoResponse user = usersCache.get(order.getUserId());
            return orderMapper.toResponse(order, user);
        });
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setStatus(request.status());
        UserInfoResponse user = userServiceAdapter.getUserInfo(order.getUserId());
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
}