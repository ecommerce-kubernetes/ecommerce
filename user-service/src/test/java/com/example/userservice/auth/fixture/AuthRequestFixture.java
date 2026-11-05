package com.example.userservice.auth.fixture;

import com.example.userservice.auth.adapter.in.web.dto.LoginRequest;

public class AuthRequestFixture {

    public static LoginRequest.LoginRequestBuilder anLoginRequest() {
        return LoginRequest.builder()
                .email("la9814@naver.com")
                .password("password1234*");
    }
}
