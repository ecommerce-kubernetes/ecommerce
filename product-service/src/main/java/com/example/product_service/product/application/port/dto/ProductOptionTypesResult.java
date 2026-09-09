package com.example.product_service.product.application.port.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductOptionTypesResult(List<ProductOptionTypeResult> optionTypes) {
    public record ProductOptionTypeResult(Long id, String name) {
    }
}
