package com.innowise.orderservice.client;

import com.innowise.orderservice.dto.UserInfoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceAdapter {
    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUserInfo")
    public UserInfoResponse getUserInfo(String userId) {
        return userServiceClient.getUserById(userId);
    }

    @SuppressWarnings("unused")
    private UserInfoResponse fallbackUserInfo(String userId, Throwable t) {
        log.warn("User service unavailable for userId: {}. Reason: {}", userId, t.getMessage());
        return new UserInfoResponse("unknown", "System", "Unavailable", userId);
    }
}
