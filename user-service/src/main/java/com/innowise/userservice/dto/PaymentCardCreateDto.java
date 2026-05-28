package com.innowise.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardCreateDto {

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotBlank(message = "Card number is mandatory")
    @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
    private String number;

    @NotBlank(message = "Card holder name is mandatory")
    private String holder;

    @NotNull(message = "Expiration date is mandatory")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}