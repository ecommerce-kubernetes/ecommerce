package com.example.order_service.api.order.application.event;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderResultCode {
    PAYMENT_READY("결제 대기"),
    OUT_OF_STOCK("재고 부족"),
    INVALID_COUPON("유효하지 않은 쿠폰"),
    TIMEOUT("주문 시간 초과"),
    COUPON_EXPIRED("만료된 쿠폰"),
    SYSTEM_ERROR("시스템 에러");

    private final String name;

    public static OrderResultCode from(OrderFailureCode orderFailureCode) {
        return switch (orderFailureCode) {
            case OUT_OF_STOCK -> OUT_OF_STOCK;
            case INVALID_COUPON -> INVALID_COUPON;
            case COUPON_EXPIRED -> COUPON_EXPIRED;
            case TIMEOUT -> TIMEOUT;
            default -> SYSTEM_ERROR;
        };
    }
}
