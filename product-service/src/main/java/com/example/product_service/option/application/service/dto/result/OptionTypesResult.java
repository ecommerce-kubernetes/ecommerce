package com.example.product_service.option.application.service.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record OptionTypesResult(
        List<OptionTypeResult> types
) {
}
