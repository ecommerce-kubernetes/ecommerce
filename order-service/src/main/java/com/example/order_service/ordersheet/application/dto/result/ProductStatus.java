package com.example.order_service.ordersheet.application.dto.result;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductStatus {
    PREPARING("판매 대기 상품"),
    ON_SALE("판매중인 상품"),
    STOP_SALE("판매 중지된 상품"),
    DELETED("삭제된 상품"),
    UNKNOWN("알 수 없음");
    private final String description;

    public static ProductStatus from(String status) {
        if (status == null) {
            return UNKNOWN;
        }
        try {
            return ProductStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
