package com.example.userservice.api.auth.domain.repository;

import com.example.userservice.api.auth.domain.model.RefreshToken;
import com.example.userservice.api.support.IncludeInfraTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenRepositoryTest extends IncludeInfraTest {

    @Autowired
    private RefreshTokenRepository repository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${token.refresh_expiration_time}")
    private long refreshTtl;


    @Nested
    @DisplayName("리프레시 토큰 저장")
    class RefreshSave {

        @Test
        @DisplayName("리프레시 토큰을 저장한다")
        void save() throws JsonProcessingException {
            //given
            RefreshToken refreshToken = RefreshToken.create(1L, "refreshToken");
            //when
            repository.save(refreshToken, refreshTtl);
            //then
            RefreshToken result = (RefreshToken) redisTemplate.opsForValue().get("RT:" + refreshToken.getUserId());
            assertThat(result)
                    .extracting(RefreshToken::getUserId, RefreshToken::getToken)
                    .containsExactly(1L, "refreshToken");
        }
    }
}
