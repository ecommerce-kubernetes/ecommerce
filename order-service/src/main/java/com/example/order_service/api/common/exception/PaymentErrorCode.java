package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode{
    PAYMENT_ALREADY_PROCEED_PAYMENT(400, "PAYMENT_001", "이미 결제된 주문입니다"),
    PAYMENT_BAD_REQUEST(400, "PAYMENT_002", "잘못된 결제 요청입니다"),
    PAYMENT_NOT_FOUND(404, "PAYMENT_003", "존재하지 않은 결제 정보입니다"),
    PAYMENT_APPROVAL_FAIL(400, "PAYMENT_001", "결제 승인이 거절되었습니다"),
    PAYMENT_INSUFFICIENT_BALANCE(400, "PAYMENT_002", "잔액이 부족합니다"),
    PAYMENT_TIMEOUT(408, "PAYMENT_005", "결제 시간이 초과되었습니다"),
    PAYMENT_SYSTEM_ERROR(500, "PAYMENT_003", "시스템 오류로 결제를 진행할 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
