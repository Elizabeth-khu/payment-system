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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment-cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #createDto.userId == authentication.principal")
    @PostMapping
    public ResponseEntity<PaymentCardResponseDto> createCard(@Valid @RequestBody PaymentCardCreateDto createDto) {
        log.info("REST request to create payment card for user: {}", createDto.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.createCard(createDto));
    }

    @PostAuthorize("hasAuthority('ROLE_ADMIN') or returnObject.body.userId == authentication.principal")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable("id") Long id) {
        log.info("REST request to get payment card by id: {}", id);
        return ResponseEntity.ok(paymentCardService.getCardById(id));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<PaymentCardResponseDto> activateCard(@PathVariable("id") Long id) {
        log.info("REST request to activate payment card with id: {}", id);
        return ResponseEntity.ok(paymentCardService.activateCard(id));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> updateCard(
            @PathVariable("id") Long id,
            @Valid @RequestBody PaymentCardUpdateDto updateDto) {
        log.info("REST request to update payment card with id: {}", id);
        return ResponseEntity.ok(paymentCardService.updateCard(id, updateDto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #userId == authentication.principal")
    @GetMapping
    public ResponseEntity<Page<PaymentCardResponseDto>> getAllCards(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        log.info("REST request to get all payment cards with filters");
        return ResponseEntity.ok(paymentCardService.getAllCards(userId, active, pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #userId == authentication.principal")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardResponseDto>> getAllCardsByUserId(@PathVariable("userId") String userId) {
        log.info("REST request to get all payment cards for user: {}", userId);
        return ResponseEntity.ok(paymentCardService.getAllCardsByUserId(userId));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCard(@PathVariable("id") Long id) {
        log.info("REST request to deactivate payment card with id: {}", id);
        paymentCardService.deactivateCard(id);
        return ResponseEntity.noContent().build();
    }
}