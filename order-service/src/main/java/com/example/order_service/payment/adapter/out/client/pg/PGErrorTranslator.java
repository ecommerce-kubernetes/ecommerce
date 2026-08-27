package com.example.order_service.payment.adapter.out.client.pg;

import com.example.order_service.payment.exception.PaymentPGPortErrorCode;

public interface PGErrorTranslator {
    PaymentPGPortErrorCode translate(String code);
}
