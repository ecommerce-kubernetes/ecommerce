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
    UNSUPPORTED_PROVIDER(HttpStatus.CONFLICT, "UNSUPPORTED_PROVIDER", "지원하지 않는 결제사 입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
