package com.example.product_service.product.application.port.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductOptionValuesResult(List<OptionValueResult> optionValues) {

    public record OptionValueResult(Long id, Long optionTypeId, String name) {}
}
