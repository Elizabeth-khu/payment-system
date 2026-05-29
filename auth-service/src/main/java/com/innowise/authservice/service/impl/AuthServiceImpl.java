package com.innowise.authservice.service.impl;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.AuthResponse;
import com.innowise.authservice.dto.RegisterRequest;
import com.innowise.authservice.entity.UserCredential;
import com.innowise.authservice.exception.InvalidTokenException;
import com.innowise.authservice.exception.UserNotFoundException;
import com.innowise.authservice.repository.UserCredentialRepository;
import com.innowise.authservice.security.JwtService;
import com.innowise.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse saveCredentials(RegisterRequest request) {
        log.info("Saving new credentials for login: {}", request.getLogin());

        if (repository.findByLogin(request.getLogin()).isPresent()) {
            log.warn("User with login {} already exists", request.getLogin());
            throw new RuntimeException("User with this login already exists");
        }

        var user = UserCredential.builder()
                .userId(request.getUserId())
                .login(request.getLogin())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        repository.save(user);
        log.info("Credentials saved successfully for user ID: {}", user.getUserId());

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse createToken(AuthRequest request) {
        log.info("Authenticating user with login: {}", request.getLogin());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
        );

        var user = repository.findByLogin(request.getLogin())
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + request.getLogin()));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        log.info("Tokens successfully generated for login: {}", request.getLogin());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String userId = jwtService.extractUserId(token);
            log.debug("Validating token for user ID: {}", userId);

            var user = repository.findByUserId(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            return jwtService.isTokenValid(token, user);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Attempting to refresh token");
        String userId = jwtService.extractUserId(refreshToken);

        if (userId != null) {
            var user = repository.findByUserId(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                log.info("Access token successfully refreshed for user ID: {}", userId);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        }
        log.warn("Invalid or expired refresh token provided");
        throw new InvalidTokenException("Invalid or expired refresh token");
    }
}