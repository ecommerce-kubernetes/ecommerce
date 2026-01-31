package com.example.userservice.api.user.service.dto.result;

import com.example.userservice.jpa.entity.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserCreateResponse {
    private Long id;
    private String email;
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private String phoneNumber;
}
