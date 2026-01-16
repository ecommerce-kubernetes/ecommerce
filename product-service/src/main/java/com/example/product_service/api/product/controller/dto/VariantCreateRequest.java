package com.example.product_service.api.product.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VariantCreateRequest {
    @Valid
    @NotEmpty(message = "상품 변형 리스트는 필수입니다")
    private List<VariantRequest> variants;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class VariantRequest {
        @NotNull(message = "가격은 필수 입니다")
        @Min(value = 100, message = "가격은 100 이상이여야 합니다")
        private Long originalPrice;
        @NotNull(message = "할인율은 필수 입니다")
        @Min(value = 0, message = "할인율은 0 이상이여야 합니다")
        @Max(value = 100, message = "할인율은 100 이하여야 합니다")
        private Integer discountRate;
        @NotNull(message = "재고 수량은 필수 입니다")
        @Min(value = 1, message = "재고 수량은 1 이상이여야 합니다")
        private Integer stockQuantity;
        @NotNull(message = "상품 변형 옵션은 필수 입니다")
        @UniqueElements(message = "중복된 옵션 종류가 포함되어 있습니다")
        private List<Long> optionValueIds;

        @Builder
        private VariantRequest(Long originalPrice, Integer discountRate, Integer stockQuantity, List<Long> optionValueIds) {
            this.originalPrice = originalPrice;
            this.discountRate = discountRate;
            this.stockQuantity = stockQuantity;
            this.optionValueIds = optionValueIds;
        }
    }

    @Builder
    private VariantCreateRequest(List<VariantRequest> variants) {
        this.variants = variants;
    }
}
