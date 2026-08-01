package com.example.order_service.order.application.port.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderCouponStatus {
    AVAILABLE("사용 가능"),
    USED("사용됨");
    private final String description;
}
