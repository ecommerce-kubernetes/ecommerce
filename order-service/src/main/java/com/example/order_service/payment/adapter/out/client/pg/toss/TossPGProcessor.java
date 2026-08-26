package com.example.order_service.payment.adapter.out.client.pg.toss;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.dto.response.pg.TossCancelResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossInquiryResponse;
import com.example.order_service.infrastructure.gateway.TossGateway;
import com.example.order_service.payment.adapter.out.client.pg.PGProcessor;
import com.example.order_service.payment.application.port.dto.PGCancelResult;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PGInquiryResult;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import lombok.RequiredArgsConstructor;
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
    public void netCancel(String paymentKey, String cancelReason) {
        executeNetCancel(paymentKey, cancelReason);
    }

    private void executeNetCancel(String paymentKey, String cancelReason) {
        try {
            tossGateway.cancelPayment(paymentKey, cancelReason, null);
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
    public PGCancelResult cancel(String paymentKey, String cancelReason) {
        TossCancelResponse response = executeCancel(paymentKey, cancelReason);
        return mapper.toCancelResult(response);
    }

    private TossCancelResponse executeCancel(String paymentKey, String cancelReason) {
        try {
            return tossGateway.cancelPayment(paymentKey, cancelReason, null);
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
    public PGInquiryResult inquiry(String paymentKey) {
        TossInquiryResponse response = executeInquiry(paymentKey);
        return mapper.toInquiryResult(response);
    }

    private TossInquiryResponse executeInquiry(String paymentKey) {
        try {
            return tossGateway.inquiryPayment(paymentKey);
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
