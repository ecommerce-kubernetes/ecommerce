package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentPGPortErrorCode  implements ErrorCode {

    UNSUPPORTED_PROVIDER(HttpStatus.CONFLICT, "UNSUPPORTED_PROVIDER", "지원하지 않는 결제사 입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
