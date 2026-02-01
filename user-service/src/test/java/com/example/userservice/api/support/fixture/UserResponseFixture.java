package com.example.userservice.api.support.fixture;

import com.example.userservice.api.user.domain.model.Gender;
import com.example.userservice.api.user.service.dto.result.UserCreateResponse;

import java.time.LocalDate;

public class UserResponseFixture {

    public static UserCreateResponse.UserCreateResponseBuilder anUserCreateResponse() {
        return UserCreateResponse.builder()
                .id(1L)
                .email("la9814@naver.com")
                .name("김이박")
                .birthDate(LocalDate.of(1999, 12, 25))
                .gender(Gender.MALE)
                .phoneNumber("010-1234-5678");
    }
}
