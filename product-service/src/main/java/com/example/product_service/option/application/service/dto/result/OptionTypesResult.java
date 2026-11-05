package com.example.product_service.option.application.service.dto.result;

import com.example.product_service.option.domain.OptionType;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionTypesResult(
        List<OptionTypeResult> types
) {

    public static OptionTypesResult from(List<OptionType> optionTypes) {
        List<OptionTypeResult> types = optionTypes.stream().map(OptionTypeResult::from).toList();
        return OptionTypesResult.builder()
                .types(types)
                .build();
    }
}
