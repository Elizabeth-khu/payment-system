package com.innowise.userservice.bootstrap;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final Faker faker = new Faker();

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Database is empty. Generating fake data...");

            IntStream.range(0, 10).forEach(i -> {
                User user = User.builder()
                        .id(UUID.randomUUID().toString())
                        .name(faker.name().firstName())
                        .surname(faker.name().lastName())
                        .birthDate(LocalDate.now().minusYears(faker.number().numberBetween(18, 60)))
                        .email(faker.internet().emailAddress())
                        .active(true)
                        .build();

                int cardsCount = faker.number().numberBetween(1, 4);
                for (int j = 0; j < cardsCount; j++) {
                    PaymentCard card = PaymentCard.builder()
                            .user(user)
                            .number(faker.finance().creditCard())
                            .holder(user.getName().toUpperCase() + " " + user.getSurname().toUpperCase())
                            .expirationDate(LocalDate.now().plusYears(faker.number().numberBetween(1, 5)))
                            .active(true)
                            .build();
                    user.getPaymentCards().add(card);
                }

                userRepository.save(user);
            });

            log.info("Successfully generated 10 users with random payment cards!");
        }
    }
}