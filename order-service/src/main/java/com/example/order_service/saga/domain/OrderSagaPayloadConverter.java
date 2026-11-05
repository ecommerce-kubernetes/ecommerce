package com.example.order_service.saga.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderSagaPayloadConverter implements AttributeConverter<OrderSagaPayload, String> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    @Override
    public String convertToDatabaseColumn(OrderSagaPayload attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("OrderSagaPayload 직렬화 실패", e);
        }
    }

    @Override
    public OrderSagaPayload convertToEntityAttribute(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(s, OrderSagaPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("OrderSagaPayload 역직렬화 실패", e);
        }
    }
}
