package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    // 결제 승인 PG 에러 코드
    PAYMENT_PG_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "PAYMENT_PG_INSUFFICIENT_BALANCE", "잔액 또는 한도가 부족합니다"),
    PAYMENT_PG_METHOD_REJECTED(HttpStatus.BAD_REQUEST, "PAYMENT_PG_METHOD_REJECTED", "결제 수단 문제로 거절 되었습니다"),
    PAYMENT_PG_POLICY_RESTRICTED(HttpStatus.BAD_REQUEST, "PAYMENT_PG_POLICY_RESTRICTED", "정책 또는 보안상 결제가 제한되었습니다"),
    PAYMENT_PG_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "PAYMENT_PG_INVALID_REQUEST", "잘못된 결제 요청이거나 세션이 만료되었습니다."),
    PAYMENT_PG_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "PAYMENT_PG_ALREADY_PROCESSED", "이미 처리된 결제입니다."),

    PAYMENT_PG_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "PAYMENT_PG_ALREADY_CANCELED", "이미 취소 및 환불 처리가 완료된 결제입니다."),
    PAYMENT_PG_INVALID_REFUND_ACCOUNT(HttpStatus.BAD_REQUEST, "PAYMENT_PG_INVALID_REFUND_ACCOUNT", "입력하신 환불 계좌 정보가 유효하지 않거나 예금주명이 일치하지 않습니다. 올바른 계좌 정보를 다시 확인해주세요."),
    PAYMENT_PG_CANCEL_REJECTED(HttpStatus.BAD_REQUEST, "PAYMENT_PG_CANCEL_REJECTED", "결제사 정책(취소 기한 초과, 부분 취소 불가 등)으로 인해 환불이 거절되었습니다. 고객센터로 문의해주세요."),

    PAYMENT_PG_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_PG_NOT_FOUND", "PG사 결제를 찾을 수 없습니다"),

    PAYMENT_PG_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_PG_SERVER_ERROR", "PG사 또는 카드사 통신중 일시적인 장애가 발생했습니다"),
    PAYMENT_PG_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_PG_CIRCUIT_OPEN", "PG사 결제 서킷 열림"),
    PAYMENT_PG_AUTH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_PG_AUTH_ERROR", "PG사 인증 설정에 문제가 발생했습니다."),
    PAYMENT_PG_UNAVAILABLE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_PG_UNAVAILABLE_ERROR", "PG사 통신 오류"),

    INVALID_PAYMENT_STATUS_FOR_FAIL(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS_FOR_FAIL", "취소 가능한 결제 상태가 아닙니다"),
    UNSUPPORTED_PAYMENT_METHOD(HttpStatus.CONFLICT, "UNSUPPORTED_PAYMENT_METHOD", "지원하지 않는 결제 방식입니다"),
    UNSUPPORTED_PAYMENT_PROVIDER(HttpStatus.CONFLICT, "UNSUPPORTED_PAYMENT_PROVIDER", "지원하지 않은 결제사 입니다"),
    EXCEEDED_REFUNDABLE_AMOUNT(HttpStatus.CONFLICT, "EXCEEDED_REFUNDABLE_AMOUNT", "환불 가능 금액을 초과했습니다"),
    INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING", "환불 요청 가능한 상태가 아닙니다"),
    INVALID_PAYMENT_STATUS_FOR_APPROVAL(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS_FOR_APPROVAL", "승인 가능한 결제 상태가 아닙니다"),
    INVALID_PAYMENT_STATUS_FOR_REFUND(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATUS_FOR_REFUND", "환불 가능한 결제 상태가 아닙니다"),
    PAYMENT_TOSS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "TOSS_SERVER_ERROR", "토스 서버 오류"),

    ORDER_NOT_PENDING(HttpStatus.CONFLICT, "ORDER_NOT_PENDING", "결제를 진행할 수 없는 주문 입니다"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "존재하지 않은 결제 정보입니다"),
    PAYMENT_NOT_READY(HttpStatus.CONFLICT, "PAYMENT_NOT_READY", "승인할 수 없는 결제입니다."),
    PAYMENT_NOT_APPROVE_PENDING(HttpStatus.CONFLICT, "PAYMENT_NOT_APPROVE_PENDING", "승인할 수 없는 결제입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "PAYMENT_AMOUNT_MISMATCH", "결제 금액이 승인 요청 금액과 일치하지 않습니다."),
    APPROVAL_AMOUNT_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "PG_APPROVAL_AMOUNT_MISMATCH", "PG 승인 금액이 일치하지 않습니다"),
    PAYMENT_AUTO_CANCELED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_AUTO_CANCELED", "시스템 오류로 인해 결제가 취소되었습니다"),
    PAYMENT_REFUND_PENDING(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_REFUND_PENDING", "오류가 발생했습니다. 영업일 내로 결제 취소 됩니다");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
