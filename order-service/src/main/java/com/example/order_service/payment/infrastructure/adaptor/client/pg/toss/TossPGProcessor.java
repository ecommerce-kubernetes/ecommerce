package com.example.order_service.payment.infrastructure.adaptor.client.pg.toss;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.gateway.TossGateway;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.infrastructure.adaptor.client.pg.PGProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TossPGProcessor implements PGProcessor {
    private final TossGateway tossGateway;
    private final TossErrorTranslator translator;
    @Override
    public PGConfirmResult confirm(Long orderId, String paymentKey, Money amount) {
        return null;
    }

    @Override
    public PaymentProvider getSupportedProvider() {
        return PaymentProvider.TOSS;
    }
}
