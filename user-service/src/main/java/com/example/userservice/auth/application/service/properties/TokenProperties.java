package com.example.userservice.auth.application.service.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
    private String secret;
}
