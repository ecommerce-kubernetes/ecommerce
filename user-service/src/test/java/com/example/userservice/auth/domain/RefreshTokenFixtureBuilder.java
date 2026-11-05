package com.example.userservice.auth.domain;

import com.example.userservice.auth.domain.context.CreateRefreshTokenContext;

import java.time.Duration;

public class RefreshTokenFixtureBuilder {

    private Long userId = 1L;
    private String token = "refresh-token";
    private Duration ttl = Duration.ofDays(7);

    public static RefreshTokenFixtureBuilder given() {
        return new RefreshTokenFixtureBuilder();
    }

    public RefreshTokenFixtureBuilder withToken(String token) {
        this.token = token;
        return this;
    }

    public RefreshToken build() {
        CreateRefreshTokenContext context = CreateRefreshTokenContext.builder()
                .userId(this.userId)
                .token(this.token)
                .ttl(ttl)
                .build();

        return RefreshToken.create(context);
    }
}
