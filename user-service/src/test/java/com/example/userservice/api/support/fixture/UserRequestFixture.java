package com.example.userservice.api.support.fixture;

import com.example.userservice.api.user.controller.dto.UserCreateRequest;

import java.time.LocalDate;

public class UserRequestFixture {

    public static UserCreateRequest.UserCreateRequestBuilder anUserCreateRequest() {
        return UserCreateRequest.builder()
                .email("la9814@naver.com")
                .password("password123*")
                .name("김이박")
                .birthDate(LocalDate.of(1999, 12, 25))
                .gender("MALE")
                .phoneNumber("010-1234-5678");
    }
}
