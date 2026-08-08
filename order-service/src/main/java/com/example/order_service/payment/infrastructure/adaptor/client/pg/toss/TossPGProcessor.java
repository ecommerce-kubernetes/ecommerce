package com.example.order_service.payment.infrastructure.adaptor.client.pg.toss;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.common.exception.external.*;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import com.example.order_service.infrastructure.gateway.TossGateway;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import com.example.order_service.payment.infrastructure.adaptor.client.pg.PGProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TossPGProcessor implements PGProcessor {
    private final TossGateway tossGateway;
    private final TossErrorTranslator translator;
    private final TossPGMapper mapper;

    @Override
    public PGConfirmResult confirm(Long orderId, String paymentKey, Money amount) {
        TossConfirmResponse response = executeConfirm(orderId, paymentKey, amount);
        return mapper.toConfirmResult(response);
    }

    private TossConfirmResponse executeConfirm(Long orderId, String paymentKey, Money amount) {
        try {
            return tossGateway.confirmPayment(orderId, paymentKey, amount.longValue());
        } catch (ExternalSystemUnavailableException e) {
            throw new PortException(PaymentPGPortErrorCode.PG_UNAVAILABLE_ERROR, "TOSS_UNAVAILABLE", "토스 통신 장애");
        } catch (ExternalCircuitBreakerException e) {
            throw new PortException(PaymentPGPortErrorCode.PG_CIRCUIT_OPEN, "TOSS_CIRCUIT_OPEN", "토스 서킷 열림");
        } catch (ExternalSystemException e) {
            PaymentPGPortErrorCode code = translator.translate(e.getErrorCode());
            throw new PortException(code, e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public PaymentProvider getSupportedProvider() {
        return PaymentProvider.TOSS;
    }
}
