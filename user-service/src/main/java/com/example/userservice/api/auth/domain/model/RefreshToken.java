package com.example.userservice.api.auth.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    private Long userId;
    private String token;

    @Builder(access = AccessLevel.PRIVATE)
    private RefreshToken(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public static RefreshToken create(Long userId, String token) {
        return RefreshToken.builder()
                .userId(userId)
                .token(token)
                .build();
    }
}
