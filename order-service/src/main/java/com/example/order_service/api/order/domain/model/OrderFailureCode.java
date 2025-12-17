package com.example.order_service.api.order.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderFailureCode {
    OUT_OF_STOCK("재고 부족"),
    COUPON_EXPIRED("쿠폰 만료"),
    POINT_SHORTAGE("포인트 부족"),
    PAYMENT_FAILED("결제 승인 거절");

    private final String name;
}
