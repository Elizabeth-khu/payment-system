package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.dto.PaymentCardUpdateDto;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment-cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PaymentCardResponseDto> createCard(@Valid @RequestBody PaymentCardCreateDto createDto) {
        log.info("REST request to create payment card for user: {}", createDto.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.createCard(createDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable Long id) {
        log.info("REST request to get payment card by id: {}", id);
        return ResponseEntity.ok(paymentCardService.getCardById(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PaymentCardResponseDto> activateCard(@PathVariable Long id) {
        log.info("REST request to activate payment card with id: {}", id);
        return ResponseEntity.ok(paymentCardService.activateCard(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody PaymentCardUpdateDto updateDto) {
        log.info("REST request to update payment card with id: {}", id);
        return ResponseEntity.ok(paymentCardService.updateCard(id, updateDto));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponseDto>> getAllCards(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        log.info("REST request to get all payment cards with filters");
        return ResponseEntity.ok(paymentCardService.getAllCards(userId, active, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardResponseDto>> getAllCardsByUserId(@PathVariable String userId) {
        log.info("REST request to get all payment cards for user: {}", userId);
        return ResponseEntity.ok(paymentCardService.getAllCardsByUserId(userId));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCard(@PathVariable Long id) {
        log.info("REST request to deactivate payment card with id: {}", id);
        paymentCardService.deactivateCard(id);
        return ResponseEntity.noContent().build();
    }
}