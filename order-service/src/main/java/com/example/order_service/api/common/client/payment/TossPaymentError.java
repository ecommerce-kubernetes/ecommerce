package com.example.order_service.api.common.client.payment;

import com.example.order_service.api.common.exception.ErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TossPaymentError {
    ALREADY_PROCESSED_PAYMENT("ALREADY_PROCESSED_PAYMENT", PaymentErrorCode.PAYMENT_ALREADY_PROCEED_PAYMENT),
    UNKNOWN_ERROR("UNKNOWN", PaymentErrorCode.PAYMENT_SYSTEM_ERROR);
    private final String tossErrorCode;
    private final ErrorCode businessErrorCode;

    public static TossPaymentError findByTossErrorCode(String tossErrorCode) {
        return Arrays.stream(values())
                .filter(e -> e.getTossErrorCode().equals(tossErrorCode))
                .findFirst()
                .orElse(UNKNOWN_ERROR);
    }
}
