package com.example.userservice.auth.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;

    public static TokenResponse of(String accessToken){
        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
