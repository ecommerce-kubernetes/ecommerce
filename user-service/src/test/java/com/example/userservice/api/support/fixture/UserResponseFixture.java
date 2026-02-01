package com.example.userservice.api.support.fixture;

import com.example.userservice.api.user.domain.model.Gender;
import com.example.userservice.api.user.service.dto.result.UserCreateResponse;
import com.example.userservice.api.user.service.dto.result.UserOrderResponse;

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

    public static UserOrderResponse.UserOrderResponseBuilder anUserOrderResponse() {
        return UserOrderResponse.builder()
                .userId(1L)
                .userName("김이박")
                .pointBalance(10000L)
                .phoneNumber("010-1234-5678");
    }
}
