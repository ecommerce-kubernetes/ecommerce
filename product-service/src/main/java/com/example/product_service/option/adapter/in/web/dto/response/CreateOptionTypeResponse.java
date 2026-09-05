package com.example.product_service.option.adapter.in.web.dto.response;

import com.example.product_service.option.application.service.dto.result.CreateOptionTypeResult;
import com.fasterxml.jackson.annotation.JsonFormat;

public record CreateOptionTypeResponse(@JsonFormat(shape = JsonFormat.Shape.STRING) Long id) {
    public static CreateOptionTypeResponse from(CreateOptionTypeResult result) {
        return new CreateOptionTypeResponse(result.id());
    }
}
