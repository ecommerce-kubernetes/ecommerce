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
        return new ProductOptionSnapshot(optionTypeName, optionValueName);
    }
}
