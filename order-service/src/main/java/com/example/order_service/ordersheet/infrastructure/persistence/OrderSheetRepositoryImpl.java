package com.example.order_service.ordersheet.infrastructure.persistence;

import com.example.order_service.ordersheet.domain.OrderSheet;
import com.example.order_service.ordersheet.domain.OrderSheetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderSheetRepositoryImpl implements OrderSheetRepository {
    private static final String PREFIX_ORDER_SHEET = "order:sheet:";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public OrderSheet save(OrderSheet orderSheet) {
        String sheetId = orderSheet.getSheetId();
        String orderSheetString = orderSheetToString(orderSheet);
        redisTemplate.opsForValue().set(PREFIX_ORDER_SHEET + sheetId, orderSheetString);
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
