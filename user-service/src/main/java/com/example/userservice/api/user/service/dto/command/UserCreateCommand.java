package com.example.userservice.api.user.service.dto.command;

import com.example.userservice.api.user.domain.model.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserCreateCommand {
    private String email;
    private String password;
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private String phoneNumber;
}
