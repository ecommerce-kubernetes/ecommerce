package com.example.product_service.api.product.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOptionSpecRequest {
    @NotNull(message = "옵션 Id 리스트는 필수 입니다")
    private List<Long> optionTypeIds;

    @Builder
    private ProductOptionSpecRequest(List<Long> optionTypeIds) {
        this.optionTypeIds = optionTypeIds;
    }
}
