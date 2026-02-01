package com.example.userservice.api.auth.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenData {
    private String accessToken;
    private String refreshToken;

    public static TokenData of(String accessToken, String refreshToken) {
        return TokenData.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
