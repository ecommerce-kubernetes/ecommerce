package com.example.userservice.auth.adapter.out.persistence;

import com.example.userservice.auth.application.port.TokenRepository;
import com.example.userservice.auth.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository implements TokenRepository {

    private static final String KEY_PREFIX = "RT:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        String key = generateKey(refreshToken.getUserId());
        redisTemplate.opsForValue().set(key, refreshToken.getToken(), refreshToken.getTtl());
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        String key = generateKey(userId);
        String token = redisTemplate.opsForValue().get(key);

        if (token == null) {
            return Optional.empty();
        }

        Duration ttl = Duration.ofSeconds(redisTemplate.getExpire(key, TimeUnit.SECONDS));

        return Optional.of(RefreshToken.reconstitute()
                .userId(userId)
                .token(token)
                .ttl(ttl)
                .build());
    }

    @Override
    public void deleteByUserId(Long userId) {
        redisTemplate.delete(generateKey(userId));
    }

    private String generateKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}
