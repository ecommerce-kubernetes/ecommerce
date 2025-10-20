package com.example.couponservice.config.specification.annotation;

import com.example.couponservice.vo.ResponseError;
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
        responseCode = "409",
        description = "리소스 충돌",
        content = @Content(
                schema = @Schema(implementation = ResponseError.class)
        )
)
public @interface ConflictApiResponse {
}
