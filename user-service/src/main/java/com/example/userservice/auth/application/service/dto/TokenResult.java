package com.example.userservice.auth.application.service.dto;

import com.example.userservice.auth.adapter.in.web.dto.TokenResponse;
import lombok.Builder;

@Builder
public record TokenResult(
        String accessToken,
        String refreshToken
) {
    public static TokenResult of(String accessToken, String refreshToken) {
        return TokenResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
