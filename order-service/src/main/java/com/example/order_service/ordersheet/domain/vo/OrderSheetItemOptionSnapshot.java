package com.example.order_service.ordersheet.domain.vo;

import lombok.Getter;

@Getter
public class OrderSheetItemOptionSnapshot {
    private String optionTypeName;
    private String optionValueName;

    private OrderSheetItemOptionSnapshot(String optionTypeName, String optionValueName) {
        this.optionTypeName = optionTypeName;
        this.optionValueName = optionValueName;
    }

    public static OrderSheetItemOptionSnapshot of(String optionTypeName, String optionValueName) {
        return new OrderSheetItemOptionSnapshot(optionTypeName, optionValueName);
    }
}
