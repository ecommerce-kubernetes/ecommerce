package com.example.userservice.api.support.fixture;

import com.example.userservice.api.auth.controller.dto.LoginRequest;

public class AuthRequestFixture {

    public static LoginRequest.LoginRequestBuilder anLoginRequest() {
        return LoginRequest.builder()
                .email("la9814@naver.com")
                .password("password1234*");
    }
}
