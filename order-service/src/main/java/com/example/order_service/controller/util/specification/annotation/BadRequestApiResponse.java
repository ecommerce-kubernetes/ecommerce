package com.example.order_service.controller.util.specification.annotation;

import com.example.order_service.common.advice.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
        )
)
public @interface BadRequestApiResponse {
}
