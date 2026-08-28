package com.example.userservice.auth.fixture;

import com.example.userservice.auth.application.service.dto.TokenData;
import com.example.userservice.auth.application.service.dto.TokenResult;

public class AuthResponseFixture {

    public static TokenResult.TokenResultBuilder anTokenResult() {
        return TokenResult.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken");
    }
}
