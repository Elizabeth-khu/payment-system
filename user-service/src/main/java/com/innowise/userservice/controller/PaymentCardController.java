package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentCardResponseDto createCard(@Valid @RequestBody PaymentCardCreateDto createDto) {
        return paymentCardService.createCard(createDto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PaymentCardResponseDto getCardById(@PathVariable Long id) {
        return paymentCardService.getCardById(id);
    }

    @GetMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentCardResponseDto> getAllCardsByUserId(@PathVariable String userId) {
        return paymentCardService.getAllCardsByUserId(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateCard(@PathVariable Long id) {
        paymentCardService.deactivateCard(id);
    }
}