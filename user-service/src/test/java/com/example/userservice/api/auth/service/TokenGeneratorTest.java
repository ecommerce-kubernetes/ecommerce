package com.example.userservice.api.auth.service;

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

public class TokenGeneratorTest extends ExcludeInfraTest {

    @Autowired
    private TokenGenerator tokenGenerator;
    @Value("${token.secret}")
    private String TEST_SECRET_KEY;
    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰을 생성한다")
    void generateTokenData(){
        //given
        //when
        TokenData tokenData = tokenGenerator.generateTokenData(1L, Role.ROLE_USER);
        //then
        assertThat(tokenData.getAccessToken()).isNotNull();
        assertThat(tokenData.getRefreshToken()).isNotNull();
        assertThat(parseToken(tokenData.getAccessToken()).getSubject()).isEqualTo(String.valueOf(1L));
        assertThat(parseToken(tokenData.getRefreshToken()).getSubject()).isEqualTo(String.valueOf(1L));

        assertThat(parseToken(tokenData.getAccessToken()).get("role")).isEqualTo(Role.ROLE_USER.name());
    }

    private Claims parseToken(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
