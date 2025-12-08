package com.example.order_service.api.support;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@ActiveProfiles("test-mock")
@SpringBootTest
public abstract class DomainServiceTestSupport {
    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;
    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;
    //TODO redisScript 사용제거
    @MockitoBean
    private RedisScript<Long> redisScript;
}
