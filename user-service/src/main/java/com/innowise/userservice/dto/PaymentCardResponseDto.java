package com.innowise.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardResponseDto {

    private Long id;
    private String userId;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Boolean active;
}