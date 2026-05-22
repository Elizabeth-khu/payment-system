package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;

import java.util.List;

public interface PaymentCardService {

    PaymentCardResponseDto createCard(PaymentCardCreateDto createDto);

    PaymentCardResponseDto getCardById(Long id);

    List<PaymentCardResponseDto> getAllCardsByUserId(String userId);

    void deactivateCard(Long id);
}