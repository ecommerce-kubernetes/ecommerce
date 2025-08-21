package com.example.product_service.service.dto;

import com.example.product_service.entity.OptionValues;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class ProductVariantCreationData {
    private Map<Long, OptionValues> optionValueById;
}
