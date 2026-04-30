package com.example.order_service.support.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class MockRedisConfig {

    @Bean
    @Primary
    public RedisTemplate<String, String> mockRedisTemplate() {
        return Mockito.mock(RedisTemplate.class);
    }
}
