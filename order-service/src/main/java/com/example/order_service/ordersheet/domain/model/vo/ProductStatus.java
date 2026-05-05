package com.example.order_service.ordersheet.domain.model.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductStatus {
    ORDERABLE("주문 가능 상품"),
    UNORDERABLE("주문 불가 상품");
    private final String description;
}
