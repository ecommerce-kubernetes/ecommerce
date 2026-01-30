package com.example.order_service.api.cart.facade.dto.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CartItemStatus {
    AVAILABLE("주문 가능"), NOT_FOUND("상품을 찾을 수 없음"), PREPARING("준비중인 상품"),
    STOP_SALE("판매 중지 상품"), DELETED("삭제된 상품");
    private final String name;
}
