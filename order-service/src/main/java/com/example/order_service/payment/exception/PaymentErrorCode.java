package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.business.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    EXCEEDED_REFUNDABLE_AMOUNT(409, "EXCEEDED_REFUNDABLE_AMOUNT", "환불 가능 금액을 초과했습니다"),
    INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING(409, "INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING", "환불 요청 가능한 상태가 아닙니다"),
    INVALID_PAYMENT_STATUS_FOR_APPROVAL(409, "INVALID_PAYMENT_STATUS_FOR_APPROVAL", "승인 가능한 결제 상태가 아닙니다"),
    INVALID_PAYMENT_STATUS_FOR_REFUND(409, "INVALID_PAYMENT_STATUS_FOR_REFUND", "환불 가능한 결제 상태가 아닙니다"),
    PAYMENT_TOSS_UNAVAILABLE_ERROR(503, "TOSS_UNAVAILABLE_ERROR", "토스 서버 오류"),
    PAYMENT_TOSS_SERVER_ERROR(500, "TOSS_SERVER_ERROR", "토스 서버 오류"),
    PAYMENT_TOSS_CLIENT_ERROR(409, "TOSS_CLIENT_ERROR", "토스 결제 서버 클라이언트 오류"),
    ORDER_NOT_PENDING(409, "ORDER_NOT_PENDING", "결제를 진행할 수 없는 주문 입니다"),
    PAYMENT_ALREADY_PROCEED_PAYMENT(400, "PAYMENT_001", "이미 결제된 주문입니다"),
    PAYMENT_BAD_REQUEST(400, "PAYMENT_002", "잘못된 결제 요청입니다"),
    PAYMENT_NOT_FOUND(404, "PAYMENT_003", "존재하지 않은 결제 정보입니다"),
    PAYMENT_APPROVAL_FORBIDDEN(403, "PAYMENT_APPROVAL_FORBIDDEN", "해당 주문에 대한 결제 승인 권한이 없습니다"),
    PAYMENT_APPROVAL_FAIL(400, "PAYMENT_004", "결제 승인이 거절되었습니다"),
    PAYMENT_AMOUNT_MISMATCH(409, "PAYMENT_AMOUNT_MISMATCH", "주문의 총 결제 금액과 결제 금액이 일치하지 않습니다"),
    PAYMENT_INSUFFICIENT_BALANCE(400, "PAYMENT_005", "잔액이 부족합니다"),
    PAYMENT_TIMEOUT(408, "PAYMENT_006", "결제 시간이 초과되었습니다"),
    PAYMENT_SYSTEM_ERROR(500, "PAYMENT_007", "시스템 오류로 결제를 진행할 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
