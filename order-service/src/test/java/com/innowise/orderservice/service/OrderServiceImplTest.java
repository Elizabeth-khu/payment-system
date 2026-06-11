package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.dto.OrderCreateRequest;
import com.innowise.orderservice.dto.OrderItemRequest;
import com.innowise.orderservice.dto.OrderResponse;
import com.innowise.orderservice.dto.UserInfoResponse;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderStatus;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UserInfoResponse mockUser;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockUser = new UserInfoResponse("user-123", "John", "Doe", "john@mail.com");
        mockOrder = Order.builder().id("order-1").userId("john@mail.com").status(OrderStatus.CREATED).build();
    }

    @Test
    void getOrderById_Success() {
        String orderId = "order-1";
        OrderResponse expectedResponse = new OrderResponse(orderId, mockUser, OrderStatus.CREATED, BigDecimal.TEN, null, null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(userServiceClient.getUserByEmail(mockOrder.getUserId())).thenReturn(mockUser);
        when(orderMapper.toResponse(mockOrder, mockUser)).thenReturn(expectedResponse);

        OrderResponse actualResponse = orderService.getOrderById(orderId);

        assertNotNull(actualResponse);
        assertEquals(orderId, actualResponse.id());
        assertEquals("John", actualResponse.user().name());

        verify(orderRepository, times(1)).findById(orderId);
        verify(userServiceClient, times(1)).getUserByEmail(anyString());
    }

    @Test
    void getOrderById_NotFound() {
        String orderId = "invalid-id";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));
        verify(userServiceClient, never()).getUserByEmail(anyString());
    }

    @Test
    void createOrder_Success() {
        OrderCreateRequest request = new OrderCreateRequest("john@mail.com", List.of(new OrderItemRequest("item-1", 2)));
        Item mockItem = Item.builder().id("item-1").price(BigDecimal.valueOf(50)).build();
        OrderResponse expectedResponse = new OrderResponse("order-1", mockUser, OrderStatus.CREATED, BigDecimal.valueOf(100), null, null);

        when(itemRepository.findById("item-1")).thenReturn(Optional.of(mockItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(userServiceClient.getUserByEmail(anyString())).thenReturn(mockUser);
        when(orderMapper.toResponse(any(Order.class), eq(mockUser))).thenReturn(expectedResponse);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(100), response.totalPrice());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deleteOrder_Success() {
        String orderId = "order-1";
        when(orderRepository.existsById(orderId)).thenReturn(true);

        orderService.deleteOrderById(orderId);

        verify(orderRepository, times(1)).deleteById(orderId);
    }
}