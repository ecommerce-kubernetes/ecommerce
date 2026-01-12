package com.example.product_service.api.product.service.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class VariantCreateResponse {
    private Long productId;
    private List<CreatedVariantResponse> variants;
}
