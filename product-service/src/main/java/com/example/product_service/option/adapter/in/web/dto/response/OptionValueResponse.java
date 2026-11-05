package com.example.product_service.option.adapter.in.web.dto.response;

import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionValueResponse(@JsonFormat(shape = JsonFormat.Shape.STRING) Long id, String name) {
    public static OptionValueResponse from(OptionTypeResult.OptionValueResult result) {
        return OptionValueResponse.builder()
                .id(result.id())
                .name(result.name())
                .build();
    }

    public static List<OptionValueResponse> from(List<OptionTypeResult.OptionValueResult> values) {
        return values.stream().map(OptionValueResponse::from).toList();
    }
}
