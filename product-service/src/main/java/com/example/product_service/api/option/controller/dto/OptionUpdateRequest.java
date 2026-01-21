package com.example.product_service.api.option.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptionUpdateRequest {
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Builder
    private OptionUpdateRequest(String name) {
        this.name = name;
    }
}
