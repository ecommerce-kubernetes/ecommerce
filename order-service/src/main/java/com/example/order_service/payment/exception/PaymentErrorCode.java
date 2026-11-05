package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.ErrorCategory;
import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    UNSUPPORTED_PAYMENT_METHOD(ErrorCategory.BUSINESS_CONFLICT, "UNSUPPORTED_PAYMENT_METHOD", "지원하지 않는 결제 방식입니다"),
    UNSUPPORTED_PAYMENT_PROVIDER(ErrorCategory.BUSINESS_CONFLICT, "UNSUPPORTED_PAYMENT_PROVIDER", "지원하지 않은 결제사 입니다"),

    ORDER_NOT_PENDING(ErrorCategory.BUSINESS_CONFLICT, "ORDER_NOT_PENDING", "결제를 진행할 수 없는 주문 입니다"),
    PAYMENT_NOT_FOUND(ErrorCategory.NOT_FOUND, "PAYMENT_NOT_FOUND", "존재하지 않은 결제 정보입니다"),
    PAYMENT_NOT_READY(ErrorCategory.BUSINESS_CONFLICT, "PAYMENT_NOT_READY", "승인할 수 없는 결제입니다."),
    PAYMENT_NOT_APPROVE_PENDING(ErrorCategory.BUSINESS_CONFLICT, "PAYMENT_NOT_APPROVE_PENDING", "승인할 수 없는 결제입니다."),

    PAYMENT_CANNOT_ABORT(ErrorCategory.BUSINESS_CONFLICT, "PAYMENT_CANNOT_ABORT", "실패 처리할 수 없는 결제 입니다."),
    PAYMENT_CANNOT_REFUND_PENDING(ErrorCategory.BUSINESS_CONFLICT, "PAYMENT_CANNOT_REFUND_PENDING", "환불 대기할 수 없는 결제 입니다."),
    PAYMENT_CANNOT_CANCEL(ErrorCategory.BUSINESS_CONFLICT, "PAYMENT_CANNOT_CANCEL", "결제를 취소 할 수 없는 결제 입니다."),
    PAYMENT_AMOUNT_MISMATCH(ErrorCategory.BUSINESS_CONFLICT, "PAYMENT_AMOUNT_MISMATCH", "결제 금액이 승인 요청 금액과 일치하지 않습니다."),
    APPROVAL_AMOUNT_MISMATCH(ErrorCategory.SYSTEM_ERROR, "PG_APPROVAL_AMOUNT_MISMATCH", "PG 승인 금액이 일치하지 않습니다"),
    CANCEL_AMOUNT_MISMATCH(ErrorCategory.SYSTEM_ERROR, "CANCEL_AMOUNT_MISMATCH", "PG 환불 금액이 일치하지 않습니다"),
    PAYMENT_AUTO_CANCELED(ErrorCategory.SYSTEM_ERROR, "PAYMENT_AUTO_CANCELED", "시스템 오류로 인해 결제가 취소되었습니다"),
    PAYMENT_REFUND_PENDING(ErrorCategory.SYSTEM_ERROR, "PAYMENT_REFUND_PENDING", "오류가 발생했습니다. 영업일 내로 결제 취소 됩니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
