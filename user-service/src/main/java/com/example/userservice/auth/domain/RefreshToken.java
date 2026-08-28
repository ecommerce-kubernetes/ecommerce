package com.example.userservice.auth.domain;

import com.example.userservice.auth.domain.context.CreateRefreshTokenContext;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    private Long userId;
    private String token;
    private Duration ttl;

    @Builder(access = AccessLevel.PRIVATE)
    private RefreshToken(Long userId, String token, Duration ttl) {
        this.userId = userId;
        this.token = token;
        this.ttl = ttl;
    }

    public static RefreshToken create(CreateRefreshTokenContext context) {
        return RefreshToken.builder()
                .userId(context.userId())
                .token(context.token())
                .ttl(context.ttl())
                .build();
    }
}
