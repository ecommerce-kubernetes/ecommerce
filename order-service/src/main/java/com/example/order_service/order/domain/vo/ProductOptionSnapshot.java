package com.example.order_service.order.domain.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
