package com.example.userservice.auth.adapter.out.persistence;

import com.example.userservice.auth.application.port.TokenRepository;
import com.example.userservice.auth.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository implements TokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return null;
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        return Optional.empty();
    }

    @Override
    public void deleteByUserId(Long userId) {

    }

}
