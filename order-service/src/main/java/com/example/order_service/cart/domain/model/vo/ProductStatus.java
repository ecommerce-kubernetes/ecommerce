package com.example.order_service.cart.domain.model.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductStatus {
    AVAILABLE("구매 가능"),
    UNAVAILABLE("판매 중지/삭제 (구매 불가)");
    private final String description;

    public static ProductStatus from(String status) {
        if (status.equals("ON_SALE")) {
            return AVAILABLE;
        }
        return UNAVAILABLE;
    }
}
