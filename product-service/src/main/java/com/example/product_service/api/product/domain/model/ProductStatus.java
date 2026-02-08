package com.example.product_service.api.product.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductStatus {
    PREPARING("판매 대기 상품"),
    ON_SALE("판매중인 상품"),
    STOP_SALE("판매 중지 상품"),
    DELETED("삭제된 상품");
    private final String description;
}
