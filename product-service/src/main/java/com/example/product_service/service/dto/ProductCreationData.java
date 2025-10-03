package com.example.product_service.service.dto;

import com.example.product_service.entity.Category;
import com.example.product_service.entity.OptionType;
import com.example.product_service.entity.OptionValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class ProductCreationData {
    private Category category;
    private Map<Long, OptionType> optionTypeById;
    private Map<Long, OptionValue> optionValueById;
}
