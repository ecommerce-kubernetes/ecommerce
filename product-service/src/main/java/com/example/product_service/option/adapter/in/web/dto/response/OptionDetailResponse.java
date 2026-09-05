package com.example.product_service.option.adapter.in.web.dto.response;

import com.example.product_service.option.application.service.dto.result.OptionResult;
import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionDetailResponse(
        Long id,
        String name,
        List<OptionValueResponse> values
) {
    public static OptionDetailResponse from(OptionResult result) {
        return OptionDetailResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .build();
    }

    private static List<OptionValueResponse> mappingValues(List<OptionTypeResult.OptionValueResult> values) {
        return values.stream().map(OptionValueResponse::from).toList();
    }

    public static List<OptionDetailResponse> from(List<OptionResult> results) {
        return results.stream().map(OptionDetailResponse::from).toList();
    }
}
