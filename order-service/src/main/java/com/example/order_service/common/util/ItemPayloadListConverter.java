package com.example.order_service.common.util;

import com.example.order_service.saga.domain.tmp.SagaPayloadDeprecated;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ItemPayloadListConverter implements AttributeConverter<List<SagaPayloadDeprecated.ItemPayload>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<SagaPayloadDeprecated.ItemPayload> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("리스트 직렬화 실패", e);
        }
    }

    @Override
    public List<SagaPayloadDeprecated.ItemPayload> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(dbData,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, SagaPayloadDeprecated.ItemPayload.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("리스트 역 직렬화 실패", e);
        }
    }
}
