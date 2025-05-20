package com.example.userservice.service;

import com.example.userservice.advice.exceptions.InvalidTokenException;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Value("${token.secret}")
    private String secret;
    @Value("${token.expiration_time}")
    private String accessExpStr;
    @Value("${token.refresh_expiration_time}")
    private String refreshExpStr;

    private static final String PREFIX = "refresh:";

    private UserRepository userRepository;
    private RedisTemplate<String, String> redisTemplate;

    public TokenService(UserRepository userRepository, RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    private SecretKey getSecretKey() {
        byte[] secretKeyBytes = Base64.getEncoder().encode(secret.getBytes());
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }

    //Redis에 리프레시 토큰 저장
    public void saveRefreshToken(Long userId, String token) {
        String key = PREFIX + userId;
        long refreshExp = Long.parseLong(refreshExpStr);
        redisTemplate.opsForValue().set(key, token, refreshExp, TimeUnit.MILLISECONDS);
    }

    //Redis에 저장된 리프레시 토큰 제거
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }

    //Access Token 생성
    public String createAccessToken(Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(Long.parseLong(accessExpStr))))
                .signWith(getSecretKey())
                .compact();
    }

    //Refresh Token 생성
    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(Long.parseLong(refreshExpStr))))
                .signWith(getSecretKey())
                .compact();
    }

    //Access + Refresh Token 묶음 생성
    public TokenPair generateTokenPair(Long userId, String role) {
        String accessToken = createAccessToken(userId, role);
        String refreshToken = createRefreshToken(userId);
        saveRefreshToken(userId, refreshToken);
        return new TokenPair(accessToken, refreshToken);

    }

    //쿠키에 리프레시 토큰 넣기
    public ResponseCookie setCookieRefreshToken(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Long.parseLong(refreshExpStr) / 1000)
                .sameSite("Strict")
                .build();
    }


    //토큰에서 사용자 ID 추출
    public Long extractUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    // 토큰 파싱 및 검증 (유효성 + Payload 추출)
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //리프레시 토큰 유효성 검증
    public boolean validateRefreshToken(Long userId, String token) {
        String key = PREFIX + userId;
        String savedToken = redisTemplate.opsForValue().get(key);

        try {
            Claims claims = parseToken(token);
            // 타입 체크
            if (!"refresh".equals(claims.get("type"))) {
                return false;
            }
            // Redis 토큰과 비교
            return token.equals(savedToken);
        } catch (JwtException e) {
            return false;
        }
    }

    //엑세스 토큰 재발급
    public String reissueAccessToken(String refreshToken) {

        Long userId = extractUserId(refreshToken);
        // 리프레시 토큰 검증
        if (!validateRefreshToken(userId, refreshToken)) {
            throw new InvalidTokenException("리프레시 토큰이 유효하지 않습니다. 다시 로그인 해주세요.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        //새로운 액세스 토큰 발급
        return createAccessToken(userId, String.valueOf(user.getRole()));
    }
}
