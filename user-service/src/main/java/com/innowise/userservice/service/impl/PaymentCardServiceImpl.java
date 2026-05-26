package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.dto.PaymentCardUpdateDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.repository.specification.PaymentCardSpecification;
import com.innowise.userservice.service.PaymentCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    @CacheEvict(value = "users", key = "#createDto.userId")
    public PaymentCardResponseDto createCard(PaymentCardCreateDto createDto) {
        log.info("Creating new payment card for user id: {}", createDto.getUserId());

        User user = userRepository.findById(createDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + createDto.getUserId()));

        if (paymentCardRepository.countByUserIdAndActiveTrue(createDto.getUserId()) >= 5) {
            throw new CardLimitExceededException("User cannot have more than 5 active cards");
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
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with id: " + id));

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
    @Transactional(readOnly = true)
    public Page<PaymentCardResponseDto> getAllCards(String userId, Boolean active, Pageable pageable) {
        log.info("Fetching all payment cards with filters - userId: {}, active: {}", userId, active);

        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasUserId(userId))
                .and(PaymentCardSpecification.isActive(active));

        return paymentCardRepository.findAll(spec, pageable)
                .map(paymentCardMapper::toResponseDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#result.userId")
    public PaymentCardResponseDto activateCard(Long id) {
        log.info("Activating payment card with id: {}", id);
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with id: " + id));

        card.setActive(true);
        PaymentCard savedCard = paymentCardRepository.save(card);

        return paymentCardMapper.toResponseDto(savedCard);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#result.userId")
    public PaymentCardResponseDto updateCard(Long id, PaymentCardUpdateDto updateDto) {
        log.info("Updating payment card with id: {}", id);
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with id: " + id));

        paymentCardMapper.updateEntityFromDto(updateDto, card);
        PaymentCard savedCard = paymentCardRepository.save(card);

        return paymentCardMapper.toResponseDto(savedCard);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#result.userId")
    public PaymentCardResponseDto deactivateCard(Long id) {
        log.info("Deactivating payment card with id: {}", id);
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with id: " + id));

        card.setActive(false);
        PaymentCard savedCard = paymentCardRepository.save(card);

        return paymentCardMapper.toResponseDto(savedCard);
    }
}