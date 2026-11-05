package com.example.product_service.option.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record AddOptionValueResponse(@JsonFormat(shape = JsonFormat.Shape.STRING) Long optionValueId) {
    public static AddOptionValueResponse of(Long optionValueId) {
        return AddOptionValueResponse.builder()
                .optionValueId(optionValueId)
                .build();
    }
}
