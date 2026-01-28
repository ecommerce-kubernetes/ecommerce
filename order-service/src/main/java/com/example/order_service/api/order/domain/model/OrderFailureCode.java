package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.common.exception.ErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderFailureCode {
    OUT_OF_STOCK("재고 부족"),
    COUPON_EXPIRED("쿠폰 만료"),
    INVALID_COUPON("유효하지 않은 쿠폰"),
    POINT_SHORTAGE("포인트 부족"),
    PAYMENT_FAILED("결제 승인 거절"),
    PAYMENT_INSUFFICIENT_BALANCE("결제 잔액 부족"),
    PAYMENT_TIMEOUT("결제 가능 시간 초과"),
    PAYMENT_NOT_FOUND("존재하지 않는 결제 정보"),
    SAGA_TIMEOUT("SAGA 타임아웃"),
    UNKNOWN("알 수 없는 오류"),
    SYSTEM_ERROR("시스템 에러"),
    ALREADY_PROCEED_PAYMENT("이미 결제된 주문");

    private final String name;


    public static OrderFailureCode fromSagaErrorCode(String errorCode) {
        return switch (errorCode) {
            case "OUT_OF_STOCK" -> OrderFailureCode.OUT_OF_STOCK;
            case "INVALID_COUPON" -> OrderFailureCode.INVALID_COUPON;
            case "INSUFFICIENT_POINT" -> OrderFailureCode.POINT_SHORTAGE;
            case "TIMEOUT" -> OrderFailureCode.SAGA_TIMEOUT;
            default -> UNKNOWN;
        };
    }
}
