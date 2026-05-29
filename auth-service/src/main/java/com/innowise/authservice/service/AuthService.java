package com.innowise.authservice.service;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.AuthResponse;
import com.innowise.authservice.dto.RegisterRequest;

public interface AuthService {
    AuthResponse saveCredentials(RegisterRequest request);
    AuthResponse createToken(AuthRequest request);
    boolean validateToken(String token);
    AuthResponse refreshToken(String refreshToken);
}