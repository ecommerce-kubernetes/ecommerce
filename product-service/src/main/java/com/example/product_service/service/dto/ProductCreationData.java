package com.example.product_service.service.dto;

import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class ProductCreationData {
    private Categories categories;
    private Map<Long, OptionTypes> optionTypeById;
    private Map<Long, OptionValues> optionValueById;
}
