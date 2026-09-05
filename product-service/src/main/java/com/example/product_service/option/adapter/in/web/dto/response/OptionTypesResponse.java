package com.example.product_service.option.adapter.in.web.dto.response;

import com.example.product_service.option.application.service.dto.result.OptionTypesResult;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionTypesResponse(List<OptionTypeResponse> optionTypes) {
    public static OptionTypesResponse from(OptionTypesResult types) {
        return OptionTypesResponse.builder()
                .optionTypes(OptionTypeResponse.from(types.types()))
                .build();
    }
}
