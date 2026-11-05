package com.example.userservice.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "token")
public record TokenProperties(
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String secret
) {
}
