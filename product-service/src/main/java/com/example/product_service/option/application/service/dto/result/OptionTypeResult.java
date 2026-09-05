package com.example.product_service.option.application.service.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record OptionTypeResult(
        Long id,
        String name,
        List<OptionValueResult> values
) {

    @Builder
    public record OptionValueResult(
            Long id,
            String name
    ) {
    }
}
