package com.example.product_service.api.product.service.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OptionSpecResponse {
    private Long productId;
    private List<ProductOptionSpecResponse> optionNames;
}
