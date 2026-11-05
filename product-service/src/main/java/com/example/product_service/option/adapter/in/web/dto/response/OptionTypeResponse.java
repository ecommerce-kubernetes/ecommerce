package com.example.product_service.option.adapter.in.web.dto.response;

import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionTypeResponse(@JsonFormat(shape = JsonFormat.Shape.STRING) Long id, String name, List<OptionValueResponse> values) {
    public static OptionTypeResponse from(OptionTypeResult type) {
        return OptionTypeResponse.builder()
                .id(type.id())
                .name(type.name())
                .values(OptionValueResponse.from(type.values()))
                .build();
    }

    public static List<OptionTypeResponse> from(List<OptionTypeResult> types) {
        return types.stream().map(OptionTypeResponse::from).toList();
    }
}
