package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.service.dto.JwtClaims;
import com.example.userservice.api.auth.service.dto.TokenData;
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

import static org.assertj.core.api.Assertions.assertThat;

class TokenGeneratorTest extends ExcludeInfraTest {

    @Autowired
    private TokenGenerator tokenGenerator;
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
        TokenData tokenData = tokenGenerator.generateTokenData(jwtPayload);
        //then
        assertThat(tokenData.getAccessToken()).isNotNull();
        assertThat(tokenData.getRefreshToken()).isNotNull();
        assertThat(parseToken(tokenData.getAccessToken()).getSubject()).isEqualTo(String.valueOf(1L));
        assertThat(parseToken(tokenData.getRefreshToken()).getSubject()).isEqualTo(String.valueOf(1L));

        Claims claims = parseToken(tokenData.getAccessToken());
        assertThat(claims)
                .containsEntry("role", Role.ROLE_USER.name());
    }

    private Claims parseToken(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
