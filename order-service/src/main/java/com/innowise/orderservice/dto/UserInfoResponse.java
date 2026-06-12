package com.innowise.orderservice.dto;

public record UserInfoResponse(
        String id,
        String name,
        String surname,
        String email
) {}