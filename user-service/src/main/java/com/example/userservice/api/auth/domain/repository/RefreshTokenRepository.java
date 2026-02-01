package com.example.userservice.api.auth.domain.repository;

import com.example.userservice.api.auth.domain.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    public void save(RefreshToken refreshToken, long ttl) {
        redisTemplate.opsForValue().set(
                "RT:" + refreshToken.getUserId(),
                refreshToken,
                ttl,
                TimeUnit.MICROSECONDS
        );
    }
}
