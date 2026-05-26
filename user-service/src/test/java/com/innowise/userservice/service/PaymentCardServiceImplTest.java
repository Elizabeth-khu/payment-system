package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    @Test
    void createCard_ShouldThrowException_WhenUserHasFiveActiveCards() {
        String userId = "user-123";
        PaymentCardCreateDto createDto = new PaymentCardCreateDto();
        createDto.setUserId(userId);

        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(5L);

        assertThatThrownBy(() -> paymentCardService.createCard(createDto))
                .isInstanceOf(CardLimitExceededException.class)
                .hasMessageContaining("User cannot have more than 5 active cards");

        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void createCard_ShouldCreateCardSuccessfully_WhenLimitNotReached() {
        String userId = "user-123";
        PaymentCardCreateDto createDto = new PaymentCardCreateDto();
        createDto.setUserId(userId);

        User mockUser = new User();
        mockUser.setId(userId);

        PaymentCard paymentCard = new PaymentCard();
        PaymentCard savedCard = new PaymentCard();
        PaymentCardResponseDto responseDto = new PaymentCardResponseDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(2L);
        when(paymentCardMapper.toEntity(createDto)).thenReturn(paymentCard);
        when(paymentCardRepository.save(paymentCard)).thenReturn(savedCard);
        when(paymentCardMapper.toResponseDto(savedCard)).thenReturn(responseDto);

        PaymentCardResponseDto result = paymentCardService.createCard(createDto);

        assertThat(result).isNotNull();
        verify(paymentCardRepository, times(1)).save(paymentCard);
    }

    @Test
    void getCardById_ShouldThrowException_WhenCardNotFound() {
        Long invalidCardId = 999L;
        when(paymentCardRepository.findById(invalidCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.getCardById(invalidCardId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment card not found");
    }

    @Test
    void deactivateCard_ShouldThrowException_WhenCardNotFound() {
        Long invalidCardId = 999L;
        when(paymentCardRepository.findById(invalidCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.deactivateCard(invalidCardId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void getAllCardsByUserId_ShouldReturnListOfCards() {
        String userId = "user-123";
        PaymentCard card = new PaymentCard();
        card.setId(1L);
        PaymentCardResponseDto dto = new PaymentCardResponseDto();
        dto.setId(1L);

        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(card));
        when(paymentCardMapper.toResponseDto(card)).thenReturn(dto);

        var result = paymentCardService.getAllCardsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(paymentCardRepository, times(1)).findAllByUserId(userId);
    }
}