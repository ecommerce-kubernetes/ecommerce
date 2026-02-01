package com.example.userservice.api.user.service.dto.result;

import com.example.userservice.api.user.domain.model.Gender;
import com.example.userservice.api.user.domain.model.User;
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

    public static UserCreateResponse from(User user) {
        return UserCreateResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
