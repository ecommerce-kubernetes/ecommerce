package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.service.dto.JwtClaims;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.common.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.support.ExcludeInfraTest;
import com.example.userservice.api.user.domain.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest extends ExcludeInfraTest {

    @Autowired
    private JwtProvider jwtProvider;
    @Value("${token.secret}")
    private String testSecretKey;

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰을 생성한다")
    void generateTokenData(){
        //given
        JwtClaims jwtPayload = JwtClaims.builder()
                .id(1L)
                .email("test@naver.com")
                .name("홍길동")
                .role(Role.ROLE_USER)
                .build();
        //when
        TokenData tokenData = jwtProvider.generateTokenData(jwtPayload);
        //then
        assertThat(tokenData.getAccessToken()).isNotNull();
        assertThat(tokenData.getRefreshToken()).isNotNull();
        assertThat(parseToken(tokenData.getAccessToken()).getSubject()).isEqualTo(String.valueOf(1L));
        assertThat(parseToken(tokenData.getRefreshToken()).getSubject()).isEqualTo(String.valueOf(1L));

        Claims claims = parseToken(tokenData.getAccessToken());
        assertThat(claims)
                .containsEntry("role", Role.ROLE_USER.name());
    }

    @Test
    @DisplayName("토큰 검증 후 claims 를 추출한다")
    void getValidClaims() {
        //given
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 100000);
        String token = genTestToken(now, expiration, testSecretKey);
        //when
        Claims claims = jwtProvider.getValidClaims(token);
        //then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("1");
    }

    @Test
    @DisplayName("토큰이 만료되면 예외가 발생한다")
    void getValidClaims_expired() {
        //given
        Date now = new Date();
        String token = genTestToken(now, now, testSecretKey);
        //when
        //then
        assertThatThrownBy(() -> jwtProvider.getValidClaims(token))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("토큰 서명이 다른 경우 예외가 발생한다")
    void getValidClaims_signatureException() {
        //given
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 100000);
        String token = genTestToken(now, expiration, "invalidKey-1dsa0f9hsagvcxizovzxc");
        //when
        //then
        assertThatThrownBy(() -> jwtProvider.getValidClaims(token))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("토큰 형식이 다른 경우 예외가 발생한다")
    void getValidClaims_malformedJwtException() {
        //given
        String token = "잘못된 형식의 토큰";
        //when
        //then
        assertThatThrownBy(() -> jwtProvider.getValidClaims(token))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }

    private Claims parseToken(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String genTestToken(Date issuedAt, Date expiration, String key) {
        SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("1")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
}
