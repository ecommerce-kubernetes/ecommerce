package com.example.userservice.auth.fixture;

import com.example.userservice.auth.application.service.dto.TokenData;

import java.time.Duration;

public class AuthResponseFixture {

    public static TokenData.TokenDataBuilder anTokenData() {
        return TokenData.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .refreshTokenTtl(Duration.ofDays(7));
    }
}
