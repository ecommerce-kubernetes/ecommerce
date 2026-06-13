package com.example.order_service.order.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionSnapshot {
    private String optionTypeName;
    private String optionValueName;

    @Builder(builderMethodName = "reconstitute")
    private ProductOptionSnapshot(String optionTypeName, String optionValueName) {
        this.optionTypeName = optionTypeName;
        this.optionValueName = optionValueName;
    }

    public static ProductOptionSnapshot of(String optionTypeName, String optionValueName) {
        if (optionTypeName == null || optionTypeName.isBlank()) {
            throw new IllegalArgumentException("상품 옵션 타입은 필수입니다");
        }
        if (optionValueName == null || optionValueName.isBlank()) {
            throw new IllegalArgumentException("상품 옵션 값은 필수입니다");
        }
        return new ProductOptionSnapshot(optionTypeName, optionValueName);
    }
}
