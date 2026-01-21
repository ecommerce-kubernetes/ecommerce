package com.example.product_service.api.product.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InternalVariantRequest {
    @NotEmpty(message = "아이디는 필수 입니다")
    private List<Long> variantIds;

    @Builder
    private InternalVariantRequest(List<Long> variantIds){
        this.variantIds = variantIds;
    }
}
