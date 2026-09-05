package com.example.order_service.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderFailureCode {
    INSUFFICIENT_STOCK("재고 부족"),
    COUPON_EXPIRED("쿠폰 만료"),
    INVALID_COUPON("유효하지 않은 쿠폰"),
    INSUFFICIENT_POINT("포인트 부족"),
    PAYMENT_INSUFFICIENT_BALANCE("결제 잔액 부족"),
    SAGA_TIMEOUT("SAGA 타임아웃"),
    UNKNOWN("알 수 없는 오류"),
    ALREADY_PROCEED_PAYMENT("이미 결제된 주문");

    private final String name;
}
