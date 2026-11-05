package com.example.userservice.user.application.service.dto.command;

import com.example.userservice.user.domain.vo.Gender;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateUserCommand(
        String email,
        String password,
        String name,
        LocalDate birthDate,
        Gender gender,
        String phoneNumber
) {
}
