package com.example.userservice.auth.application.service;

import com.example.userservice.auth.application.port.dto.AuthUserResult;
import com.example.userservice.auth.application.service.dto.TokenData;
import com.example.userservice.auth.exception.AuthErrorCode;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.properties.TokenProperties;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final TokenProperties tokenProperties;

    public TokenData generateTokenData(AuthUserResult user) {
        Date now = new Date();

        long accessTtlMillis = tokenProperties.accessTokenTtl().toMillis();
        long refreshTtlMillis = tokenProperties.refreshTokenTtl().toMillis();

        Date accessExpiration = new Date(now.getTime() + accessTtlMillis);
        Date refreshExpiration = new Date(now.getTime() + refreshTtlMillis);

        String accessToken = genAccessToken(user, now, accessExpiration);
        String refreshToken = genRefreshToken(user.id(), now, refreshExpiration);

        return TokenData.of(accessToken, refreshToken, tokenProperties.refreshTokenTtl());
    }

    public Long getUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
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
        return Keys.hmacShaKeyFor(tokenProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    private String genAccessToken(AuthUserResult user, Date date, Date expiration){
        return Jwts.builder()
                .subject(String.valueOf(user.id()))
                .issuer("buynest-user-service")
                .claim("email", user.email())
                .claim("name", user.name())
                .claim("role", user.role().name())
                .claim("token_type", "ACCESS")
                .issuedAt(date)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    private String genRefreshToken(Long userId, Date date, Date expiration) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer("buynest-user-service")
                .claim("token_type", "REFRESH")
                .issuedAt(date)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }
}
