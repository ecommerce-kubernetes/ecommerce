package com.example.userservice.auth.domain.context;

import lombok.Builder;
import org.springframework.util.Assert;

import java.time.Duration;

@Builder
public record CreateRefreshTokenContext(
        Long userId,
        String token,
        Duration expiresAt
) {
    public CreateRefreshTokenContext {
        Assert.notNull(userId, "리프레시 토큰 생성시 유저 아이디는 필수이다.");
        Assert.notNull(token, "리프레시 토큰 생성시 토큰 값은 필수이다.");
        Assert.notNull(expiresAt, "리프레시 토큰 생성시 만료 기간은 필수이다.");
    }
}
