package com.innowise.userservice.dto;

import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    private String name;
    private String surname;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
}
