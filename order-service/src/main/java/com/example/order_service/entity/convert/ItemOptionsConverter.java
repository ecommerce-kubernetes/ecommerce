package com.example.order_service.entity.convert;

import com.example.order_service.api.cart.infrastructure.client.dto.ItemOption;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class ItemOptionsConverter implements AttributeConverter<List<ItemOption>, String> {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<ItemOption> attribute) {
        if(attribute == null) return null;
        try{
            return mapper.writeValueAsString(attribute);
        } catch (Exception e){
            throw new RuntimeException("Fail to converter", e);
        }
    }

    @Override
    public List<ItemOption> convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isEmpty()) return null;
        try{
            return mapper.readValue(dbData, new TypeReference<List<ItemOption>>() {});
        } catch (Exception e){
            throw new RuntimeException("Failed to convert JSON to ItemOption list.", e);
        }
    }
}
