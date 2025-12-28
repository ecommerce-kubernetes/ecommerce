package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.common.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderFailureCode {
    OUT_OF_STOCK("재고 부족"),
    COUPON_EXPIRED("쿠폰 만료"),
    INVALID_COUPON("유효하지 않은 쿠폰"),
    POINT_SHORTAGE("포인트 부족"),
    PAYMENT_FAILED("결제 승인 거절"),
    PAYMENT_INSUFFICIENT_BALANCE("결제 잔액 부족"),
    PAYMENT_TIMEOUT("결제 가능 시간 초과"),
    TIMEOUT("SAGA 타임아웃"),
    UNKNOWN("알 수 없는 오류");

    private final String name;

    public static OrderFailureCode from (PaymentErrorCode code) {
        return switch (code) {
            case APPROVAL_FAIL -> PAYMENT_FAILED;
            case INSUFFICIENT_BALANCE -> PAYMENT_INSUFFICIENT_BALANCE;
            case EXPIRED -> PAYMENT_TIMEOUT;
            default -> PAYMENT_FAILED;
        };
    }
}
