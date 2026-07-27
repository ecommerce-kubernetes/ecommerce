package com.example.order_service.order.infrastructure.adaptor.persistence;

import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.domain.model.OrderSheet;
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

    @Override
    public OrderSheet save(OrderSheet orderSheet, Duration ttl) {
        return orderSheet;
    }

    @Override
    public Optional<OrderSheet> findById(String orderSheetId) {
       return Optional.empty();
    }

    @Override
    public Optional<OrderSheet> findByIdAndOrdererId(String orderSheetId, Long ordererId) {
        return Optional.empty();
    }

}
