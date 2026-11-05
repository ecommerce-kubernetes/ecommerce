package com.example.product_service.option.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

public record CreateOptionTypeResponse(@JsonFormat(shape = JsonFormat.Shape.STRING) Long optionTypeId) {
    public static CreateOptionTypeResponse of(Long optionTypeId) {
        return new CreateOptionTypeResponse(optionTypeId);
    }
}
