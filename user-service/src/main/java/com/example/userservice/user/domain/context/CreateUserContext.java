package com.example.userservice.user.domain.context;

import com.example.userservice.user.domain.vo.Gender;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateUserContext(
        String email,
        String password,
        String name,
        LocalDate birthDate,
        Gender gender,
        String phoneNumber
) {
}
