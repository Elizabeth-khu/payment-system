package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        String invalidId = "wrong-id";
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserById_ShouldReturnUser_WhenPaymentCardsListIsEmpty() {
        String userId = "user-1";
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setPaymentCards(new ArrayList<>());

        UserResponseDto mockResponse = new UserResponseDto();
        mockResponse.setId(userId);
        mockResponse.setPaymentCards(new ArrayList<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponseDto(mockUser)).thenReturn(mockResponse);

        UserResponseDto result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentCards()).isEmpty();
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_ShouldReturnUser_WhenCardsAreInactive() {
        String userId = "user-2";

        PaymentCard inactiveCard = new PaymentCard();
        inactiveCard.setId(100L);
        inactiveCard.setActive(false);

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setPaymentCards(List.of(inactiveCard));

        UserResponseDto mockResponse = new UserResponseDto();
        mockResponse.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponseDto(mockUser)).thenReturn(mockResponse);

        UserResponseDto result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(userId);
    }
}