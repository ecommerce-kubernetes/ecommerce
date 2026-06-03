package com.example.order_service.common.util;

import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class LongListConverter implements AttributeConverter<List<Long>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new InvalidDomainValueException("리스트 직렬화 실패");
        }
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(dbData,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, Long.class));
        } catch (JsonProcessingException e) {
            throw new InvalidDomainValueException("리스트 역 직렬화 실패");
        }
    }
}
