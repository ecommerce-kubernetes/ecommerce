package com.example.order_service.ordersheet.domain.model.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheetItemOptionSnapshot {
    private String optionTypeName;
    private String optionValueName;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItemOptionSnapshot(String optionTypeName, String optionValueName) {
        this.optionTypeName = optionTypeName;
        this.optionValueName = optionValueName;
    }

    public static OrderSheetItemOptionSnapshot of(String optionTypeName, String optionValueName) {
        return new OrderSheetItemOptionSnapshot(optionTypeName, optionValueName);
    }
}
