package com.example.product_service.option.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record UpdateOptionValueResponse(@JsonFormat(shape = JsonFormat.Shape.STRING) Long optionValueId) {

    public static UpdateOptionValueResponse of(Long optionValueId) {
        return UpdateOptionValueResponse.builder()
                .optionValueId(optionValueId)
                .build();
    }
}
