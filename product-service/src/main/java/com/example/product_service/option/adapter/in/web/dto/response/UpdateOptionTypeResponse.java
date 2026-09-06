package com.example.product_service.option.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record UpdateOptionTypeResponse(@JsonFormat(shape = JsonFormat.Shape.STRING) Long optionTypeId) {
    public static UpdateOptionTypeResponse of(Long optionTypeId) {
        return UpdateOptionTypeResponse.builder()
                .optionTypeId(optionTypeId)
                .build();
    }
}
