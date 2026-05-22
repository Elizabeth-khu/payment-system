package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    public PaymentCardResponseDto createCard(PaymentCardCreateDto createDto) {
        log.info("Creating new payment card for user id: {}", createDto.getUserId());

        User user = userRepository.findById(createDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + createDto.getUserId()));

        long existingCardsCount = paymentCardRepository.countByUserId(user.getId());
        if (existingCardsCount >= 5) {
            log.error("User {} has reached the maximum limit of 5 payment cards", user.getId());
            throw new IllegalStateException("User cannot have more than 5 active cards");
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(createDto);
        paymentCard.setUser(user);

        PaymentCard savedCard = paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toResponseDto(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentCardResponseDto getCardById(Long id) {
        log.info("Fetching payment card by id: {}", id);
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment card not found with id: " + id));
        return paymentCardMapper.toResponseDto(paymentCard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentCardResponseDto> getAllCardsByUserId(String userId) {
        log.info("Fetching all payment cards for user id: {}", userId);
        return paymentCardRepository.findAllByUserId(userId).stream()
                .map(paymentCardMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void deactivateCard(Long id) {
        log.info("Deactivating payment card with id: {}", id);
        if (!paymentCardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment card not found with id: " + id);
        }
        paymentCardRepository.deactivateCardByIdNative(id);
    }
}