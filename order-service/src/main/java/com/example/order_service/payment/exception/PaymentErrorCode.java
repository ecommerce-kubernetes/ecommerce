package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.application.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_TOSS_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_TOSS_CIRCUIT_OPEN", "토스 결제 서킷 열림"),
    INVALID_PAYMENT_STATUS_FOR_FAIL(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS_FOR_FAIL", "취소 가능한 결제 상태가 아닙니다"),
    UNSUPPORTED_PAYMENT_METHOD(HttpStatus.CONFLICT, "UNSUPPORTED_PAYMENT_METHOD", "지원하지 않는 결제 방식입니다"),
    EXCEEDED_REFUNDABLE_AMOUNT(HttpStatus.CONFLICT, "EXCEEDED_REFUNDABLE_AMOUNT", "환불 가능 금액을 초과했습니다"),
    INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING", "환불 요청 가능한 상태가 아닙니다"),
    INVALID_PAYMENT_STATUS_FOR_APPROVAL(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS_FOR_APPROVAL", "승인 가능한 결제 상태가 아닙니다"),
    INVALID_PAYMENT_STATUS_FOR_REFUND(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS_FOR_REFUND", "환불 가능한 결제 상태가 아닙니다"),
    PAYMENT_TOSS_UNAVAILABLE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "TOSS_UNAVAILABLE_ERROR", "토스 서버 오류"),
    PAYMENT_TOSS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "TOSS_SERVER_ERROR", "토스 서버 오류"),
    PAYMENT_TOSS_CLIENT_ERROR(HttpStatus.CONFLICT, "TOSS_CLIENT_ERROR", "토스 결제 서버 클라이언트 오류"),
    ORDER_NOT_PENDING(HttpStatus.CONFLICT, "ORDER_NOT_PENDING", "결제를 진행할 수 없는 주문 입니다"),
    PAYMENT_ALREADY_PROCEED_PAYMENT(HttpStatus.BAD_REQUEST, "PAYMENT_001", "이미 결제된 주문입니다"),
    PAYMENT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "PAYMENT_002", "잘못된 결제 요청입니다"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_003", "존재하지 않은 결제 정보입니다"),
    PAYMENT_APPROVAL_FORBIDDEN(HttpStatus.FORBIDDEN, "PAYMENT_APPROVAL_FORBIDDEN", "해당 주문에 대한 결제 승인 권한이 없습니다"),
    PAYMENT_APPROVAL_FAIL(HttpStatus.BAD_REQUEST, "PAYMENT_004", "결제 승인이 거절되었습니다"),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "PAYMENT_AMOUNT_MISMATCH", "주문의 총 결제 금액과 결제 금액이 일치하지 않습니다"),
    PG_APPROVAL_AMOUNT_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "PG_APPROVAL_AMOUNT_MISMATCH", "PG 승인 금액이 일치하지 않습니다"),
    PAYMENT_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "PAYMENT_005", "잔액이 부족합니다"),
    PAYMENT_AUTO_CANCELED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_AUTO_CANCELED", "시스템 오류로 인해 결제가 취소되었습니다"),
    PAYMENT_REFUND_PENDING(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_REFUND_PENDING", "오류가 발생했습니다. 영업일 내로 결제 취소 됩니다"),
    PAYMENT_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "PAYMENT_006", "결제 시간이 초과되었습니다"),
    PAYMENT_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_007", "시스템 오류로 결제를 진행할 수 없습니다");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
