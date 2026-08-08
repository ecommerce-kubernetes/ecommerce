package com.example.order_service.payment.infrastructure.adaptor.client;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.domain.PaymentProvider;
import org.springframework.stereotype.Service;

@Service
public class PaymentPGAdaptor implements PaymentPGPort {

    @Override
    public PGConfirmResult confirm(Long orderId, String paymentKey, Money amount, PaymentProvider provider) {
        return null;
    }
}
