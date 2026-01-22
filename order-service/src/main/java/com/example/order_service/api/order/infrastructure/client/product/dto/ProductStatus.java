package com.example.order_service.api.order.infrastructure.client.product.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductStatus {
    PREPARING("판매 대기 상품"),
    ON_SALE("판매중인 상품"),
    STOP_SALE("판매 중지 상품"),
    DELETED("삭제된 상품");
    private final String description;
}
