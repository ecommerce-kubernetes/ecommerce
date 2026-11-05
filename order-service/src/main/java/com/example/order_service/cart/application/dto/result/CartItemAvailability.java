package com.example.order_service.cart.application.dto.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CartItemAvailability {
    AVAILABLE("판매 가능"),
    NOT_FOR_SALE("판매 불가"),
    LACK_OF_STOCK("수량 부족");
    private final String description;
}
