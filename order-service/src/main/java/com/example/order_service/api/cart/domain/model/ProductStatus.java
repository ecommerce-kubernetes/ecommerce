package com.example.order_service.api.cart.domain.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum ProductStatus {
    PREPARING("판매 대기 상품"),
    ON_SALE("판매중인 상품"),
    STOP_SALE("판매 중지 상품"),
    DELETED("삭제된 상품"),
    UNKNOWN("알 수 없음");
    private final String status;

    public static ProductStatus from (String status) {
        if (status == null || status.isBlank()) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(type -> type.name().equals(status.toUpperCase()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
