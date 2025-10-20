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
        responseCode = "404",
        description = "리소스를 찾지 못함",
        content = @Content(
                schema = @Schema(implementation = ResponseError.class)
        )
)
public @interface NotFoundApiResponse {
}
