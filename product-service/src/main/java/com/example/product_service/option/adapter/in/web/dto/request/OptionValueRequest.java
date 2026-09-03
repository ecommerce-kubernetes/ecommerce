package com.example.product_service.option.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record OptionValueRequest(
        @NotBlank(message = "옵션 값 이름은 필수 입니다")
        String name
) { }
