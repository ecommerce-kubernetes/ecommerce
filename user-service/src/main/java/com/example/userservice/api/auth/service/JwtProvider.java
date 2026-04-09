package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.service.dto.JwtClaims;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.auth.service.properties.TokenProperties;
import com.example.userservice.api.common.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    private final TokenProperties tokenProperties;

    public TokenData generateTokenData(JwtClaims claims) {
        Date now = new Date();
        String accessToken = genAccessToken(claims, now);
        String refreshToken = genRefreshToken(claims.getId(), now);
        return TokenData.of(accessToken, refreshToken);
    }

    public long getRefreshTokenExpiration() {
        return tokenProperties.getRefreshExpirationTime();
    }

    public Claims getValidClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (SignatureException e) {
            log.warn("토큰 시그니처 오류");
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        } catch (MalformedJwtException e) {
            log.warn("토큰 형식 오류");
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private String genAccessToken(JwtClaims claims, Date date){
        return Jwts.builder()
                .subject(String.valueOf(claims.getId()))
                .issuer("buynest-user-service")
                .claim("email", claims.getEmail())
                .claim("name", claims.getName())
                .claim("role", claims.getRole().name())
                .claim("token_type", "ACCESS")
                .issuedAt(date)
                .expiration(new Date(date.getTime() + tokenProperties.getExpirationTime()))
                .signWith(getSecretKey())
                .compact();
    }

    private String genRefreshToken(Long userId, Date date) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer("buynest-user-service")
                .claim("token_type", "REFRESH")
                .issuedAt(date)
                .expiration(new Date(date.getTime() + tokenProperties.getRefreshExpirationTime()))
                .signWith(getSecretKey())
                .compact();
    }
}
