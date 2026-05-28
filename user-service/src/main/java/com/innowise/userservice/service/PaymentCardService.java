package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.dto.PaymentCardUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardService {

    PaymentCardResponseDto createCard(PaymentCardCreateDto createDto);

    PaymentCardResponseDto getCardById(Long id);

    List<PaymentCardResponseDto> getAllCardsByUserId(String userId);

    Page<PaymentCardResponseDto> getAllCards(String userId, Boolean active, Pageable pageable);

    PaymentCardResponseDto activateCard(Long id);

    PaymentCardResponseDto updateCard(Long id, PaymentCardUpdateDto updateDto);

    PaymentCardResponseDto deactivateCard(Long id);
}