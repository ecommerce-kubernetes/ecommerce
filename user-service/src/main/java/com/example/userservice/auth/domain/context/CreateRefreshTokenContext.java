package com.example.userservice.auth.domain.context;

import lombok.Builder;

import java.time.Duration;

@Builder
public record CreateRefreshTokenContext(
        Long userId,
        String token,
        Duration ttl
) {
}
