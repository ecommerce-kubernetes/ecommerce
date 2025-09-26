package com.example.order_service.entity.convert;

import com.example.order_service.dto.response.ItemOptionResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class ItemOptionsConverter implements AttributeConverter<List<ItemOptionResponse>, String> {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<ItemOptionResponse> attribute) {
        if(attribute == null) return null;
        try{
            return mapper.writeValueAsString(attribute);
        } catch (Exception e){
            throw new RuntimeException("Fail to converter", e);
        }
    }

    @Override
    public List<ItemOptionResponse> convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isEmpty()) return null;
        try{
            return mapper.readValue(dbData, new TypeReference<List<ItemOptionResponse>>() {});
        } catch (Exception e){
            throw new RuntimeException("Failed to convert JSON to ItemOption list.", e);
        }
    }
}
