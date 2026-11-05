package com.example.userservice.auth.adapter.out.persistence;

import com.example.userservice.auth.domain.RefreshToken;
import com.example.userservice.auth.domain.context.CreateRefreshTokenContext;
import com.example.userservice.support.annotation.MockKafka;
import com.example.userservice.support.annotation.WithRedis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@MockKafka
@WithRedis
class TokenRedisRepositoryTest {

    private static final String KEY_PREFIX = "RT:";

    @Autowired
    private TokenRedisRepository tokenRedisRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("리프레시 토큰을 redis에 저장한다.")
    void save() {
        //given
        Long userId = 1L;
        Duration ttl = Duration.ofDays(14);
        RefreshToken refreshToken = createRefreshToken(userId, "refresh-token-value", ttl);
        //when
        tokenRedisRepository.save(refreshToken);
        //then
        String key = KEY_PREFIX + userId;
        String savedValue = stringRedisTemplate.opsForValue().get(key);

        assertThat(savedValue).isEqualTo("refresh-token-value");

        Long expireSeconds = stringRedisTemplate.getExpire(key);
        assertThat(expireSeconds).isGreaterThan(0L);
        assertThat(expireSeconds).isLessThanOrEqualTo(ttl.getSeconds());
    }

    @Test
    @DisplayName("유저 아이디로 리프레시 토큰을 조회한다.")
    void findByUserId() {
        //given
        Long userId = 2L;
        Duration ttl = Duration.ofDays(14);
        RefreshToken refreshToken = createRefreshToken(userId, "refresh-token-value", ttl);
        tokenRedisRepository.save(refreshToken);
        //when
        Optional<RefreshToken> findRefreshToken = tokenRedisRepository.findByUserId(userId);
        //then
        assertThat(findRefreshToken).isPresent();
        assertThat(findRefreshToken.get().getUserId()).isEqualTo(userId);
        assertThat(findRefreshToken.get().getToken()).isEqualTo("refresh-token-value");
    }

    @Test
    @DisplayName("저장된 리프레시 토큰이 없으면 빈 Optional이 반환된다.")
    void findByUserId_whenTokenNotFound_thenReturnsEmpty() {
        //given
        //when
        Optional<RefreshToken> findRefreshToken = tokenRedisRepository.findByUserId(999L);
        //then
        assertThat(findRefreshToken).isEmpty();
    }

    @Test
    @DisplayName("유저 아이디로 리프레시 토큰을 삭제한다.")
    void deleteByUserId() {
        //given
        Long userId = 3L;
        Duration ttl = Duration.ofDays(14);
        RefreshToken refreshToken = createRefreshToken(userId, "refresh-token-value", ttl);
        tokenRedisRepository.save(refreshToken);
        //when
        tokenRedisRepository.deleteByUserId(userId);
        //then
        Optional<RefreshToken> findRefreshToken = tokenRedisRepository.findByUserId(userId);
        assertThat(findRefreshToken).isEmpty();
    }

    private RefreshToken createRefreshToken(Long userId, String token, Duration ttl) {
        CreateRefreshTokenContext context = CreateRefreshTokenContext.builder()
                .userId(userId)
                .token(token)
                .ttl(ttl)
                .build();
        return RefreshToken.create(context);
    }
}
