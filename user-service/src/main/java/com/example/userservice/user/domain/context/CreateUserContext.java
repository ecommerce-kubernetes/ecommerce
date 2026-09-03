package com.example.userservice.user.domain.context;

import com.example.userservice.user.domain.vo.Gender;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateUserContext(
        Long id,
        String email,
        String encryptedPassword,
        String name,
        LocalDate birthDate,
        Gender gender,
        String phoneNumber
) {
}
