package com.example.userservice.auth.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record TokenResponse(
        String accessToken
) {
    public static TokenResponse of(String accessToken){
        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
