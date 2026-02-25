package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.service.properties.TokenProperties;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.user.domain.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenGenerator {

    private final TokenProperties tokenProperties;

    public TokenData generateTokenData(Long userId, Role role) {
        Date now = new Date();
        String accessToken = genAccessToken(userId, role, now);
        String refreshToken = genRefreshToken(userId, now);
        return TokenData.of(accessToken, refreshToken);
    }

    public long getRefreshTokenExpiration() {
        return tokenProperties.getRefreshExpirationTime();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private String genAccessToken(Long userId, Role role, Date date){
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .issuedAt(date)
                .expiration(new Date(date.getTime() + tokenProperties.getExpirationTime()))
                .signWith(getSecretKey())
                .compact();
    }

    private String genRefreshToken(Long userId, Date date) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "REFRESH")
                .issuedAt(date)
                .expiration(new Date(date.getTime() + tokenProperties.getRefreshExpirationTime()))
                .signWith(getSecretKey())
                .compact();
    }
}
