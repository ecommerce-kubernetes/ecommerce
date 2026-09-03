package com.example.product_service.option.adapter.in.web.dto.response;

import com.example.product_service.option.application.service.dto.result.OptionValueResult;
import lombok.Builder;

@Builder
public record OptionValueResponse(
        Long id,
        String name
) {
    public static OptionValueResponse from(OptionValueResult result) {
        return OptionValueResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .build();
    }
}
