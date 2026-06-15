package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserServiceAdapter;
import com.innowise.orderservice.dto.OrderCreateRequest;
import com.innowise.orderservice.dto.OrderItemRequest;
import com.innowise.orderservice.dto.OrderResponse;
import com.innowise.orderservice.dto.OrderUpdateRequest;
import com.innowise.orderservice.dto.UserInfoResponse;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.exception.DuplicateOrderException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceAdapter userServiceAdapter;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UserInfoResponse mockUser;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockUser = new UserInfoResponse("user-123", "John", "Doe", "john@mail.com");
        mockOrder = Order.builder()
                .id("order-1")
                .userId("user-123")
                .idempotencyKey("idemp-key-123")
                .status(OrderStatus.CREATED)
                .build();
    }

    @Test
    void createOrder_Success() {
        OrderCreateRequest request = new OrderCreateRequest("user-123", List.of(new OrderItemRequest("item-1", 2)));
        Item mockItem = Item.builder().id("item-1").price(BigDecimal.valueOf(50)).build();
        OrderResponse expectedResponse = new OrderResponse("order-1", mockUser, OrderStatus.CREATED, BigDecimal.valueOf(100), null, null);

        when(orderRepository.existsByIdempotencyKey("idemp-key-123")).thenReturn(false);
        when(itemRepository.findById("item-1")).thenReturn(Optional.of(mockItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(userServiceAdapter.getUserInfo(anyString())).thenReturn(mockUser);
        when(orderMapper.toResponse(any(Order.class), eq(mockUser))).thenReturn(expectedResponse);

        OrderResponse response = orderService.createOrder("idemp-key-123", request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(100), response.totalPrice());
        verify(orderRepository, times(1)).save(any(Order.class));
    }


    @Test
    void createOrder_DuplicateIdempotencyKey_ThrowsException() {
        OrderCreateRequest request = new OrderCreateRequest("user-123", List.of(new OrderItemRequest("item-1", 2)));

        when(orderRepository.existsByIdempotencyKey("duplicate-key")).thenReturn(true);

        assertThrows(DuplicateOrderException.class, () -> orderService.createOrder("duplicate-key", request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_Success() {
        String orderId = "order-1";
        OrderResponse expectedResponse = new OrderResponse(orderId, mockUser, OrderStatus.CREATED, BigDecimal.TEN, null, null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(userServiceAdapter.getUserInfo(mockOrder.getUserId())).thenReturn(mockUser);
        when(orderMapper.toResponse(mockOrder, mockUser)).thenReturn(expectedResponse);

        OrderResponse actualResponse = orderService.getOrderById(orderId);

        assertNotNull(actualResponse);
        assertEquals(orderId, actualResponse.id());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        String orderId = "invalid-id";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));
        verify(userServiceAdapter, never()).getUserInfo(anyString());
    }

    @Test
    void updateOrderStatus_Success() {
        OrderUpdateRequest request = new OrderUpdateRequest(OrderStatus.DELIVERED);
        OrderResponse expectedResponse = new OrderResponse("order-1", mockUser, OrderStatus.DELIVERED, BigDecimal.TEN, null, null);

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(mockOrder));
        when(userServiceAdapter.getUserInfo(mockOrder.getUserId())).thenReturn(mockUser);
        when(orderMapper.toResponse(mockOrder, mockUser)).thenReturn(expectedResponse);

        OrderResponse response = orderService.updateOrderStatus("order-1", request);

        assertNotNull(response);
        assertEquals(OrderStatus.DELIVERED, response.status());
    }

    @Test
    void updateOrderStatus_NotFound_ThrowsException() {
        OrderUpdateRequest request = new OrderUpdateRequest(OrderStatus.DELIVERED);
        when(orderRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.updateOrderStatus("invalid", request));
    }

    @Test
    void deleteOrderById_Success() {
        String orderId = "order-1";
        when(orderRepository.existsById(orderId)).thenReturn(true);

        orderService.deleteOrderById(orderId);

        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void deleteOrderById_NotFound_ThrowsException() {
        String orderId = "invalid-id";

        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrderById(orderId));
        verify(orderRepository, never()).deleteById(anyString());
    }

    @Test
    void createOrder_DuplicateIdempotencyKey() {
        OrderCreateRequest request = new OrderCreateRequest("user-1", List.of());
        when(orderRepository.existsByIdempotencyKey("dup-key")).thenReturn(true);

        assertThrows(DuplicateOrderException.class, () -> orderService.createOrder("dup-key", request));
    }

    @Test
    void updateOrderStatus_NotFound() {
        when(orderRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.updateOrderStatus("invalid", new OrderUpdateRequest(OrderStatus.DELIVERED)));
    }

    @Test
    void deleteOrderById_NotFound() {
        when(orderRepository.existsById("invalid")).thenReturn(false);
        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrderById("invalid"));
    }

    @Test
    void getOrders_WithFilters() {
        Page<Order> mockPage = new org.springframework.data.domain.PageImpl<>(List.of(mockOrder));
        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Order>>any(), any(Pageable.class))).thenReturn(mockPage);
        when(userServiceAdapter.getUserInfo(mockOrder.getUserId())).thenReturn(mockUser);

        Page<OrderResponse> result = orderService.getOrders("user-123", null, null, null, org.springframework.data.domain.Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrders_WithFilters_Success() {
        Page<Order> mockPage = new PageImpl<>(List.of(mockOrder));
        OrderResponse expectedResponse = new OrderResponse("order-1", mockUser, OrderStatus.CREATED, BigDecimal.TEN, null, null);

        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Order>>any(), any(Pageable.class))).thenReturn(mockPage);
        when(userServiceAdapter.getUserInfo(mockOrder.getUserId())).thenReturn(mockUser);
        when(orderMapper.toResponse(mockOrder, mockUser)).thenReturn(expectedResponse);

        Page<OrderResponse> result = orderService.getOrders("user-123", null, null, List.of(OrderStatus.CREATED), Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userServiceAdapter, times(1)).getUserInfo(anyString());
    }

    @Test
    void getOrders_EmptyResult_Success() {
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList());

        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Order>>any(), any(Pageable.class))).thenReturn(emptyPage);

        Page<OrderResponse> result = orderService.getOrders(null, null, null, null, Pageable.unpaged());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userServiceAdapter, never()).getUserInfo(anyString());
    }
}