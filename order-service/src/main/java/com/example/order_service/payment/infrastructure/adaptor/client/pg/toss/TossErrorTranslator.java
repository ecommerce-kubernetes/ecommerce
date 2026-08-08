package com.example.order_service.payment.infrastructure.adaptor.client.pg.toss;

import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import com.example.order_service.payment.infrastructure.adaptor.client.pg.PGErrorTranslator;
import org.springframework.stereotype.Component;

@Component
public class TossErrorTranslator implements PGErrorTranslator {
    @Override
    public PaymentPGPortErrorCode translate(String code) {
        return null;
    }
}
