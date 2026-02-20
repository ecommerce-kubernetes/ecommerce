package com.example.userservice.api.auth.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
    private long expirationTime;
    private long refreshExpirationTime;
    private String secret;
}
