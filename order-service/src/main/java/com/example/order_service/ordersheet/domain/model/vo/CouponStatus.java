package com.example.order_service.ordersheet.domain.model.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CouponStatus {
    SUCCESS("사용 가능"),
    UNAVAILABLE("사용 불가"),
    EXPIRED("사용 기간 만료"),
    MINIMUM_ORDER_AMOUNT_NOT_MET("최소 결제금액을 만족하지 않음");
    private final String description;
}
