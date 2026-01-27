package com.example.order_service.api.common.client.payment;

import com.example.order_service.api.common.exception.ErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TossPaymentErrorCode {
    //400 에러
    ALREADY_PROCESSED_PAYMENT("ALREADY_PROCESSED_PAYMENT", PaymentErrorCode.PAYMENT_ALREADY_PROCEED_PAYMENT),
    INVALID_REQUEST("INVALID_REQUEST", PaymentErrorCode.PAYMENT_BAD_REQUEST),
    INVALID_API_KEY("INVALID_API_KEY", PaymentErrorCode.PAYMENT_SYSTEM_ERROR),

    //401 에러
    UNAUTHORIZED_KEY("UNAUTHORIZED_KEY", PaymentErrorCode.PAYMENT_SYSTEM_ERROR),

    //403 에러
    REJECT_ACCOUNT_PAYMENT("REJECT_ACCOUNT_PAYMENT", PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE),
    FORBIDDEN_REQUEST("FORBIDDEN_REQUEST", PaymentErrorCode.PAYMENT_SYSTEM_ERROR),

    //404 에러
    NOT_FOUND_PAYMENT("NOT_FOUND_PAYMENT", PaymentErrorCode.PAYMENT_NOT_FOUND),
    NOT_FOUND_PAYMENT_SESSION("NOT_FOUND_PAYMENT_SESSION", PaymentErrorCode.PAYMENT_TIMEOUT),

    UNKNOWN_ERROR("UNKNOWN", PaymentErrorCode.PAYMENT_SYSTEM_ERROR);
    private final String tossErrorCode;
    private final ErrorCode businessErrorCode;

    public static TossPaymentErrorCode findByTossErrorCode(String tossErrorCode) {
        return Arrays.stream(values())
                .filter(e -> e.getTossErrorCode().equals(tossErrorCode))
                .findFirst()
                .orElse(UNKNOWN_ERROR);
    }
}
