package com.example.order_service.ordersheet.infrastructure.persistence;

import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.repository.OrderSheetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderSheetRepositoryImpl implements OrderSheetRepository {
    private static final String PREFIX_ORDER_SHEET = "order:sheet:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OrderSheetRedisMapper redisMapper;

    @Override
    public OrderSheet save(OrderSheet orderSheet, Duration ttl) {
        String key = PREFIX_ORDER_SHEET + orderSheet.getSheetId();
        OrderSheetRedisEntity entity = redisMapper.toEntity(orderSheet);
        String value = entityToString(entity);
        redisTemplate.opsForValue().set(key, value, ttl);
        return orderSheet;
    }

    @Override
    public Optional<OrderSheet> findById(String sheetId) {
        String key = PREFIX_ORDER_SHEET + sheetId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        OrderSheetRedisEntity entity = stringToEntity(value);
        return Optional.of(redisMapper.toDomain(entity));
    }

    private String entityToString(OrderSheetRedisEntity entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("엔티티 변환 실패");
        }
    }

    private OrderSheetRedisEntity stringToEntity(String str) {
        try {
            return objectMapper.readValue(str, OrderSheetRedisEntity.class);
        } catch (JsonProcessingException e){
            throw new RuntimeException("엔티티 변환 실패");
        }
    }
}
