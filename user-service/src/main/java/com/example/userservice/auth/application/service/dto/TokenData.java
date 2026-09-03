package com.example.userservice.auth.application.service.dto;

import lombok.Builder;

import java.time.Duration;

@Builder
public record TokenData(
        String accessToken,
        String refreshToken,
        Duration refreshTokenTtl
) {

    public static TokenData of(String accessToken, String refreshToken, Duration refreshTokenTtl) {
        return TokenData.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenTtl(refreshTokenTtl)
                .build();
    }
}
