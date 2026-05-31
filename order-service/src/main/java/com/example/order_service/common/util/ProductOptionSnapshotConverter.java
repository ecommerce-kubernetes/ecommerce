package com.example.order_service.common.util;

import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ProductOptionSnapshotConverter implements AttributeConverter<List<ProductOptionSnapshot>,String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ProductOptionSnapshot> productOptionSnapshots) {
        if (productOptionSnapshots == null || productOptionSnapshots.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(productOptionSnapshots);
        } catch (JsonProcessingException e){
            throw new InvalidDomainValueException("옵션 직렬화 실패");
        }
    }

    @Override
    public List<ProductOptionSnapshot> convertToEntityAttribute(String s) {
        if (s == null || s.isBlank()){
            return List.of();
        }
        try {
            return objectMapper.readValue(s,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, ProductOptionSnapshot.class));
        } catch (JsonProcessingException e) {
            throw new InvalidDomainValueException("옵션 역 직렬화 실패");
        }
    }
}
