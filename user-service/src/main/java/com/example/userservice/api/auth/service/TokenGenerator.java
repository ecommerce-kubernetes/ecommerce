package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.user.domain.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenGenerator {

    @Value("${token.expiration_time}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${token.refresh_expiration_time}")
    private long REFRESH_TOKEN_EXPIRATION;
    @Value("${token.secret}")
    private String keyString;

    private SecretKey secretKey;

    @PostConstruct
    public void init(){
        this.secretKey = Keys.hmacShaKeyFor(keyString.getBytes(StandardCharsets.UTF_8));
    }

    public TokenData generateTokenData(Long userId, Role role) {
        Date now = new Date();
        log.info("AccessToken Expiration = {}", ACCESS_TOKEN_EXPIRATION);
        String accessToken = genAccessToken(userId, role, now);
        String refreshToken = genRefreshToken(userId, now);
        return TokenData.of(accessToken, refreshToken);
    }

    public long getRefreshTokenExpiration() {
        return REFRESH_TOKEN_EXPIRATION;
    }

    private String genAccessToken(Long userId, Role role, Date date){
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .issuedAt(date)
                .expiration(new Date(date.getTime() + ACCESS_TOKEN_EXPIRATION))
                .signWith(secretKey)
                .compact();
    }

    private String genRefreshToken(Long userId, Date date) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "REFRESH")
                .issuedAt(date)
                .expiration(new Date(date.getTime() + REFRESH_TOKEN_EXPIRATION))
                .signWith(secretKey)
                .compact();
    }
}
