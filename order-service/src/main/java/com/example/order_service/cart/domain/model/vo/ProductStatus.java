package com.example.order_service.cart.domain.model.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductStatus {
    AVAILABLE("구매 가능"),
    UNAVAILABLE("판매 중지/삭제 (구매 불가)");
    private final String description;
}
