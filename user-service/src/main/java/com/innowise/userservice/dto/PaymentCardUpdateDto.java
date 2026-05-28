package com.innowise.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardUpdateDto {

    @NotBlank(message = "Card holder name cannot be blank")
    private String holder;

    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}