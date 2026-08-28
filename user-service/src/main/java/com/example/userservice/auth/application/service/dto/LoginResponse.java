package com.example.userservice.auth.application.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;

    public static LoginResponse of(String accessToken){
        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
