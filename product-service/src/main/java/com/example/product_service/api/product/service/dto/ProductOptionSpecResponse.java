package com.example.product_service.api.product.service.dto;

import lombok.Getter;

@Getter
public class ProductOptionSpecResponse {
    private Long optionTypeId;
    private String name;
    private Integer priority;
}
