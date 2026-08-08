package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentPGPortErrorCode  implements ErrorCode {

    PG_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PG_SERVER_ERROR", "PG사 또는 카드사 통신중 일시적인 장애가 발생했습니다."),
    PG_UNAVAILABLE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "PG_UNAVAILABLE_ERROR", "PG사 통신중 오류가 발생했습니다."),
    PG_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "PG_CIRCUIT_OPEN", "PG사 통신이 원활하지 않습니다."),

    PG_INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "PG_INSUFFICIENT_BALANCE", "잔액 또는 한도가 부족합니다."),
    PG_METHOD_REJECTED(HttpStatus.BAD_REQUEST, "PG_METHOD_REJECTED", "결제 수단 문제로 거절 되었습니다"),
    PG_POLICY_RESTRICTED(HttpStatus.BAD_REQUEST, "PG_POLICY_RESTRICTED", "정책 또는 보안상 결제가 제한되었습니다"),
    PG_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "PG_INVALID_REQUEST", "잘못된 결제 요청이거나 세션이 만료되었습니다."),
    PG_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "PG_ALREADY_PROCESSED", "이미 처리된 결제입니다."),

    PG_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "PG_ALREADY_CANCELED", "이미 취소 및 환불 처리가 완료된 결제입니다."),
    PG_INVALID_REFUND_ACCOUNT(HttpStatus.BAD_REQUEST, "PG_INVALID_REFUND_ACCOUNT", "입력하신 환불 계좌 정보가 유효하지 않거나 예금주명이 일치하지 않습니다. 올바른 계좌 정보를 다시 확인해주세요."),
    PG_CANCEL_REJECTED(HttpStatus.BAD_REQUEST, "PG_CANCEL_REJECTED", "결제사 정책(취소 기한 초과, 부분 취소 불가 등)으로 인해 환불이 거절되었습니다. 고객센터로 문의해주세요."),

    PG_AUTH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PG_AUTH_ERROR", "PG사 인증 설정에 문제가 발생했습니다."),
    PG_NOT_FOUND(HttpStatus.NOT_FOUND, "PG_NOT_FOUND", "PG사 결제를 찾을 수 없습니다"),

    UNSUPPORTED_PROVIDER(HttpStatus.CONFLICT, "UNSUPPORTED_PROVIDER", "지원하지 않는 결제사 입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
