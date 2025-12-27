package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode {
    APPROVAL_FAIL("결제 승인 실패"),
    INVALID_STATUS("주문 상태 불일치"),
    INSUFFICIENT_BALANCE("잔액 부족");

    private final String name;
}
