package com.innowise.userservice.controller;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentCardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @BeforeEach
    public void setup() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldCreatePaymentCardAndReturn201() throws Exception {

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName("Card");
        user.setSurname("Tester");
        user.setEmail("card.tester@example.com");
        user.setBirthDate(LocalDate.of(1995, 5, 5));
        user.setActive(true);
        userRepository.save(user);

        String cardJson = """
                {
                    "userId": "%s",
                    "number": "1234567812345678",
                    "holder": "CARD TESTER",
                    "expirationDate": "2030-12-31"
                }
                """.formatted(user.getId());

        mockMvc.perform(post("/api/v1/payment-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("1234567812345678"))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    public void shouldNotAllowMoreThanFiveCardsForOneUser() throws Exception {

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName("Limit");
        user.setSurname("Tester");
        user.setEmail("limit.tester@example.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setActive(true);
        userRepository.save(user);

        String cardJson = """
                {
                    "userId": "%s",
                    "number": "1234567812345678",
                    "holder": "LIMIT TESTER",
                    "expirationDate": "2030-12-31"
                }
                """.formatted(user.getId());

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/payment-cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardJson))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/v1/payment-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson))
                .andExpect(status().isBadRequest());
    }
}