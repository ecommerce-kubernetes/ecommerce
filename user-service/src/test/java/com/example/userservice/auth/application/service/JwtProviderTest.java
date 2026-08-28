package com.example.userservice.auth.application.service;

import com.example.userservice.auth.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.auth.application.port.dto.AuthUserResult;
import com.example.userservice.auth.application.service.dto.TokenData;
import com.example.userservice.auth.application.service.properties.TokenProperties;
import com.example.userservice.auth.fixture.AuthUserResultFixture;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final String SECRET = "test-jwt-provider-secret-key-1234567890-abcdefgh";
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(30);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);

    private final TokenProperties tokenProperties = createTokenProperties();
    private final JwtProvider jwtProvider = new JwtProvider(tokenProperties);

    @Test
    @DisplayName("사용자 정보로 액세스 토큰과 리프레시 토큰을 생성한다.")
    void generateTokenData() {
        //given
        AuthUserResult user = AuthUserResultFixture.anAuthUserResult().build();
        //when
        TokenData tokenData = jwtProvider.generateTokenData(user);
        //then
        assertThat(tokenData.accessToken()).isNotBlank();
        assertThat(tokenData.refreshToken()).isNotBlank();
        assertThat(tokenData.refreshTokenTtl()).isEqualTo(REFRESH_TOKEN_TTL);
    }

    @Test
    @DisplayName("유효한 토큰인 경우 유저 아이디를 반환한다.")
    void getUserId(){
        //given
        String validToken = generateToken(
                SECRET,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 10000)
        );
        //when
        Long userId = jwtProvider.getUserId(validToken);
        //then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("만료된 토큰을 검증하면 예외가 발생한다.")
    void getUserId_whenTokenExpired_thenThrownException() {
        //given
        String expiredToken = generateToken(
                SECRET,
                new Date(System.currentTimeMillis() - 20_000),
                new Date(System.currentTimeMillis() - 10_000)
        );
        //when
        //then
        assertThatThrownBy(() -> jwtProvider.getUserId(expiredToken))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰을 검증하면 예외가 발생한다.")
    void getUserId_whenSignatureInvalid_thenThrownException() {
        //given
        String tokenSignedWithOtherKey = generateToken(
                "another-secret-key-that-is-completely-different-1234567890",
                new Date(),
                new Date(System.currentTimeMillis() + 60_000)
        );
        //when
        //then
        assertThatThrownBy(() -> jwtProvider.getUserId(tokenSignedWithOtherKey))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("형식이 잘못된 토큰을 검증하면 예외가 발생한다.")
    void getUserId_whenTokenMalformed_thenThrownException() {
        //given
        String malformedToken = "malformed-token";
        //when
        //then
        assertThatThrownBy(() -> jwtProvider.getUserId(malformedToken))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }

    private TokenProperties createTokenProperties() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenTtl(ACCESS_TOKEN_TTL);
        properties.setRefreshTokenTtl(REFRESH_TOKEN_TTL);
        return properties;
    }

    private String generateToken(String secret, Date issuedAt, Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("1")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}
