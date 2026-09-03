package com.example.product_service.product.adapter.in.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Builder(toBuilder = true)
public record ProductVariantDetailRequest(
        @NotNull(message = "가격은 필수 입니다")
        @Min(value = 100, message = "가격은 100 이상이여야 합니다")
        Long originalPrice,
        @NotNull(message = "할인율은 필수 입니다")
        @Min(value = 0, message = "할인율은 0 이상이여야 합니다")
        @Max(value = 100, message = "할인율은 100 이하여야 합니다")
        Integer discountRate,
        @NotNull(message = "재고 수량은 필수 입니다")
        @Min(value = 1, message = "재고 수량은 1 이상이여야 합니다")
        Integer stockQuantity,
        @NotNull(message = "상품 변형 옵션은 필수 입니다")
        @UniqueElements(message = "중복된 옵션 종류가 포함되어 있습니다")
        List<Long> optionValueIds
) { }
