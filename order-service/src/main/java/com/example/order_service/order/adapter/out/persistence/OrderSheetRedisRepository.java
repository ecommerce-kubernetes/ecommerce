package com.example.order_service.order.adapter.out.persistence;

import com.example.order_service.order.adapter.out.client.mapper.OrderSheetRedisMapper;
import com.example.order_service.order.adapter.out.persistence.entity.OrderSheetRedisEntity;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderSheetRedisRepository implements OrderSheetRepository {
    private static final String PREFIX_ORDER_SHEET = "order:sheet:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OrderSheetRedisMapper mapper;

    @Override
    public OrderSheet save(OrderSheet orderSheet, Duration ttl) {
        OrderSheetRedisEntity entity = mapper.toEntity(orderSheet);
        String stringEntity = parseEntityToString(entity);

        redisTemplate.opsForValue().set(generateKey(entity.getId()), stringEntity, ttl);
        return orderSheet;
    }

    @Override
    public Optional<OrderSheet> findById(Long orderSheetId) {
        String stringEntity = redisTemplate.opsForValue().get(generateKey(orderSheetId));

        if (stringEntity == null) {
            return Optional.empty();
        }

        OrderSheetRedisEntity entity = parseStringToEntity(stringEntity);
        return Optional.of(mapper.toDomain(entity));
    }

    @Override
    public Optional<OrderSheet> findByIdAndOrdererId(Long orderSheetId, Long ordererId) {
        return findById(orderSheetId)
                .filter(domain -> domain.getOrderer().getUserId().equals(ordererId));
    }

    private String generateKey(Long id) {
        return PREFIX_ORDER_SHEET + id;
    }

    private String parseEntityToString(OrderSheetRedisEntity entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("주문서 엔티티를 String 파싱 중 오류 발생");
        }
    }

    private OrderSheetRedisEntity parseStringToEntity(String stringEntity) {
        try {
            return objectMapper.readValue(stringEntity, OrderSheetRedisEntity.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("String을 주문서 엔티티로 파싱중 오류 발생");
        }
    }
}
