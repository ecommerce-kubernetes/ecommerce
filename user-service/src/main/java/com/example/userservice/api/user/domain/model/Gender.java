package com.example.userservice.api.user.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("남자"), FEMALE("여자");


    public static Gender from(String gender) {
        if (gender == null || gender.isBlank()) {
            return MALE;
        }

        return Arrays.stream(values())
                .filter(type -> type.name().equals(gender.toUpperCase()))
                .findFirst()
                .orElse(MALE);
    }

    private final String desc;
}
