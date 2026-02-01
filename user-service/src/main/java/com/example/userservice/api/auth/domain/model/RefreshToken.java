package com.example.userservice.api.auth.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    private String token;

    @Builder(access = AccessLevel.PRIVATE)
    private RefreshToken(String token) {
        this.token = token;
    }

    public static RefreshToken create(String token) {
        return RefreshToken.builder()
                .token(token)
                .build();
    }
}
