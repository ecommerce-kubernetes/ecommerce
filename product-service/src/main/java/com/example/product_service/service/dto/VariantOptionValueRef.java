package com.example.product_service.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VariantOptionValueRef {
    private Long optionTypeId;
    private Long optionValueId;
}
