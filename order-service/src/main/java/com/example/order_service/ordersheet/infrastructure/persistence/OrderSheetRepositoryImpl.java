package com.example.order_service.ordersheet.infrastructure.persistence;

import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.repository.OrderSheetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class OrderSheetRepositoryImpl implements OrderSheetRepository {
    private static final String PREFIX_ORDER_SHEET = "order:sheet:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public OrderSheet save(OrderSheet orderSheet, Duration ttl) {
        String key = PREFIX_ORDER_SHEET + orderSheet.getSheetId();
        String value = orderSheetToString(orderSheet);
        redisTemplate.opsForValue().set(key, value, ttl);
        return orderSheet;
    }

    private String orderSheetToString(OrderSheet orderSheet) {
        try {
            return objectMapper.writeValueAsString(orderSheet);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("orderSheet 변환 실패");
        }
    }
}
