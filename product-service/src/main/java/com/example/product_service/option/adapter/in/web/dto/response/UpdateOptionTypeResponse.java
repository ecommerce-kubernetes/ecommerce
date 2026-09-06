package com.example.product_service.option.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record UpdateOptionTypeResponse(@JsonFormat(shape = JsonFormat.Shape.STRING) Long id) {
    public static UpdateOptionTypeResponse of(Long id) {
        return UpdateOptionTypeResponse.builder()
                .id(id)
                .build();
    }
}
