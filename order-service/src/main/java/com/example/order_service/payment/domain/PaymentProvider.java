package com.example.order_service.payment.domain;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.exception.PaymentErrorCode;

public enum PaymentProvider {
    TOSS;

    public static PaymentProvider from(String provider) {
        return switch (provider) {
            case "TOSS" -> PaymentProvider.TOSS;
            default -> throw new BusinessException(PaymentErrorCode.UNSUPPORTED_PAYMENT_PROVIDER);
        };
    }
}
