package com.example.userservice.service;

import com.example.userservice.jpa.entity.Role;
import com.example.userservice.jpa.entity.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.TokenPair;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService 단위 테스트")
class TokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private TokenService tokenService;

    private final String secret = "my-very-secret-key-which-is-long-enough!!";
    private final String accessExpStr = "3600000"; // 1 hour
    private final String refreshExpStr = "86400000"; // 1 day

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(userRepository, redisTemplate);

        ReflectionTestUtils.setField(tokenService, "secret", secret);
        ReflectionTestUtils.setField(tokenService, "accessExpStr", accessExpStr);
        ReflectionTestUtils.setField(tokenService, "refreshExpStr", refreshExpStr);
    }

    @Test
    @DisplayName("Access Token 생성 시 사용자 ID와 Role 포함")
    void createAccessToken_containsUserIdAndRole() {
        String token = tokenService.createAccessToken(1L, "USER");

        Claims claims = tokenService.parseToken(token);
        assertEquals("1", claims.getSubject());
        assertEquals("USER", claims.get("role"));
    }

    @Test
    @DisplayName("Refresh Token 생성 시 type=refresh 포함")
    void createRefreshToken_containsTypeRefresh() {
        String token = tokenService.createRefreshToken(1L);

        Claims claims = tokenService.parseToken(token);
        assertEquals("1", claims.getSubject());
        assertEquals("refresh", claims.get("type"));
    }

    @Test
    @DisplayName("Access Token으로부터 사용자 ID 추출")
    void extractUserId_fromToken() {
        String token = tokenService.createAccessToken(123L, "ADMIN");

        Long userId = tokenService.extractUserId(token);
        assertEquals(123L, userId);
    }

    @Test
    @DisplayName("토큰 페어 생성 시 Refresh Token이 Redis에 저장됨")
    void generateTokenPair_savesRefreshTokenInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        TokenPair pair = tokenService.generateTokenPair(5L, "USER");

        assertNotNull(pair.getAccessToken());
        assertNotNull(pair.getRefreshToken());
        verify(valueOps).set(
                eq("refresh:5"),
                eq(pair.getRefreshToken()),
                eq(Long.parseLong(refreshExpStr)),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("유효한 Refresh Token 검증 성공")
    void validateRefreshToken_validToken_returnsTrue() {
        String token = tokenService.createRefreshToken(10L);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("refresh:10")).thenReturn(token);

        boolean result = tokenService.validateRefreshToken(10L, token);
        assertTrue(result);
    }

    @Test
    @DisplayName("Access Token으로 검증 시 false 반환")
    void validateRefreshToken_wrongType_returnsFalse() {
        String token = tokenService.createAccessToken(10L, "USER");

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("refresh:10")).thenReturn(token);

        boolean result = tokenService.validateRefreshToken(10L, token);
        assertFalse(result);
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 Access Token 재발급")
    void reissueAccessToken_validRefreshToken_returnsNewAccessToken() {
        Long userId = 15L;
        UserEntity mockUser = UserEntity.builder()
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(mockUser, "id", userId);

        String refreshToken = tokenService.createRefreshToken(userId);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("refresh:15")).thenReturn(refreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        String newAccessToken = tokenService.reissueAccessToken(refreshToken);
        assertNotNull(newAccessToken);

        Claims claims = tokenService.parseToken(newAccessToken);
        assertEquals("15", claims.getSubject());
    }

    @Test
    @DisplayName("Refresh Token을 HttpOnly, Secure 쿠키로 설정")
    void setCookieRefreshToken_returnsHttpOnlySecureCookie() {
        String token = "mock-refresh-token";

        ResponseCookie cookie = tokenService.setCookieRefreshToken(token);
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("refresh_token", cookie.getName());
        assertEquals(token, cookie.getValue());
    }
}