package com.example.order_service.cart.application.external.dto.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CartProductStatus {
    PREPARING("판매 대기 상품"),
    ON_SALE("판매중"),
    STOP_SALE("판매 중지 상품"),
    DELETED("삭제된 상품"),
    UNKNOWN("알 수 없음");
    private final String description;
}
