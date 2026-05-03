package com.example.order_service.order.application.dto.result;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CouponValidationStatus {

    SUCCESS("사용 가능 쿠폰"),
    MINIMUM_AMOUNT_NOT_MET("최소 할인 금액 미충족"),
    EXPIRED("쿠폰 만료"),
    UNAVAILABLE("사용 불가");
    private final String description;

    public static CouponValidationStatus from(String status) {
        if (status.equals("SUCCESS")) {
            return SUCCESS;
        } else if (status.equals("MINIMUM_AMOUNT_NOT_MET")) {
            return MINIMUM_AMOUNT_NOT_MET;
        } else if (status.equals("EXPIRED")) {
            return EXPIRED;
        } else {
            return UNAVAILABLE;
        }
    }
}
