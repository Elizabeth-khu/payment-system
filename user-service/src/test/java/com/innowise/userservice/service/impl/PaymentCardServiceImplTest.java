package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    @Test
    public void shouldCreateCardSuccessfullyWhenLimitNotReached() {

        String userId = "user-uuid";
        PaymentCardCreateDto createDto = PaymentCardCreateDto.builder()
                .userId(userId)
                .number("1111222233334444")
                .holder("JOHN DOE")
                .build();

        User mockUser = new User();
        mockUser.setId(userId);

        PaymentCard mockCardEntity = new PaymentCard();
        PaymentCard mockSavedCard = new PaymentCard();
        PaymentCardResponseDto expectedResponse = new PaymentCardResponseDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(3L);
        when(paymentCardMapper.toEntity(createDto)).thenReturn(mockCardEntity);
        when(paymentCardRepository.save(mockCardEntity)).thenReturn(mockSavedCard);
        when(paymentCardMapper.toResponseDto(mockSavedCard)).thenReturn(expectedResponse);

        PaymentCardResponseDto actualResponse = paymentCardService.createCard(createDto);

        assertNotNull(actualResponse);
        verify(userRepository).findById(userId);
        verify(paymentCardRepository).save(mockCardEntity);
    }

    @Test
    public void shouldThrowExceptionWhenUserHasAlreadyFiveCards() {

        String userId = "user-uuid";
        PaymentCardCreateDto createDto = PaymentCardCreateDto.builder()
                .userId(userId)
                .number("1111222233334444")
                .build();

        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(5L);

        assertThrows(IllegalStateException.class, () -> {
            paymentCardService.createCard(createDto);
        });

        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

}