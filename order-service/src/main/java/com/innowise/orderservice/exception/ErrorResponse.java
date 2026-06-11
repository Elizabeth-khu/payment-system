package com.innowise.orderservice.exception;

public record ErrorResponse(
        int status,
        String message,
        long timestamp
) {}