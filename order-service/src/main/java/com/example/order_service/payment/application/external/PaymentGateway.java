package com.example.order_service.payment.application.external;

import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.infrastructure.gateway.TossGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgErrorTranslator;
import com.example.order_service.payment.application.external.mapper.PgMapper;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.exception.PaymentPortException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Deprecated
public class PaymentGateway {
    private final TossGateway tossGateway;
    private final PgMapper pgMapper;
    private final PgErrorTranslator errorTranslator;

    public PGPaymentResult.Approval confirm(PGPaymentCommand.Confirm command) {
        TossClientResponse.Confirm confirm = executeExternalCall(() ->
                tossGateway.confirmPayment(command.orderNo(), command.paymentKey(), command.amount().longValue()));
        return pgMapper.toResult(confirm);
    }

    public PGPaymentResult.Cancellation cancel(PGPaymentCommand.Cancel command) {
        TossClientResponse.Cancel cancel = executeExternalCall(() -> {
            Long cancelAmount = command.amount() == null ? null : command.amount().longValue();
            return tossGateway.cancelPayment(command.paymentKey(), command.cancelReason(), cancelAmount);
        });
        return pgMapper.toResult(cancel);
    }

    public PGPaymentResult.Inquiry inquire(String paymentKey) {
        TossClientResponse.Inquiry inquiry = executeExternalCall(() ->
                tossGateway.inquirePayment(paymentKey));
        return pgMapper.toResult(inquiry);
    }

    private <T> T executeExternalCall(Supplier<T> call) {
        try {
            return call.get();
        } catch (ExternalCircuitBreakerException e) {
            throw new PaymentPortException(PaymentErrorCode.PAYMENT_PG_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new PaymentPortException(PaymentErrorCode.PAYMENT_PG_UNAVAILABLE_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemException e) {
            String code = e.getErrorCode();
            String message = e.getMessage();
            PaymentErrorCode errorCode = errorTranslator.translate(code);
            throw new PaymentPortException(errorCode, code, message);
        }
    }
}
