package com.example.product_service.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductOptionTypeCommand {
    private Long optionTypeId;
    private int priority;
    private boolean activate;
}
