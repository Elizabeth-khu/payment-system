package com.innowise.userservice;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User user = User.builder()
                    .id("auth0|eliza-test-123")
                    .name("Eliza")
                    .surname("Kh")
                    .email("eliza@test.com")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .build();

            PaymentCard card = PaymentCard.builder()
                    .number("1234-5678-9012-3456")
                    .holder("ELIZA KH")
                    .expirationDate(LocalDate.of(2030, 12, 31))
                    .user(user)
                    .build();

            user.setPaymentCards(List.of(card));
            userRepository.save(user);
            System.out.println(">>> Test User and Card saved successfully!");
        }
    }
}