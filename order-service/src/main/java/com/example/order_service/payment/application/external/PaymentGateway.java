package com.example.order_service.payment.application.external;

import com.example.order_service.common.exception.application.GatewayRejectException;
import com.example.order_service.common.exception.application.PaymentUnknownStateException;
import com.example.order_service.common.exception.external.*;
import com.example.order_service.infrastructure.adaptor.TossAdaptor;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgErrorTranslator;
import com.example.order_service.payment.application.external.mapper.PgMapper;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentGateway {
    private final TossAdaptor tossAdaptor;
    private final PgMapper pgMapper;
    private final PgErrorTranslator errorTranslator;

    public PGPaymentResult.Approval confirm(PGPaymentCommand.Confirm command) {
        TossClientResponse.Confirm confirm = fetchTossConfirm(command);
        return pgMapper.toResult(confirm);
    }

    private TossClientResponse.Confirm fetchTossConfirm(PGPaymentCommand.Confirm command) {
        try {
            return tossAdaptor.confirmPayment(command.orderNo(), command.paymentKey(), command.amount().longValue());
        } catch (ExternalCircuitBreakerException e) {
            throw new GatewayRejectException(PaymentErrorCode.PAYMENT_TOSS_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new PaymentUnknownStateException(PaymentErrorCode.PAYMENT_TOSS_UNAVAILABLE_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemException e) {
            String code = e.getErrorCode();
            String message = e.getMessage();
            PaymentErrorCode errorCode = errorTranslator.translate(code);
            if (errorCode == PaymentErrorCode.PAYMENT_PG_AUTH_ERROR ||
                    errorCode == PaymentErrorCode.PAYMENT_PG_SERVER_ERROR) {
                throw new PaymentUnknownStateException(errorCode, code, message);
            }
            throw new GatewayRejectException(errorCode, code, message);
        }
    }

    public PGPaymentResult.Cancellation cancel(PGPaymentCommand.Cancel command) {
        TossClientResponse.Cancel cancel = fetchTossCancel(command);
        return pgMapper.toResult(cancel);
    }

    private TossClientResponse.Cancel fetchTossCancel(PGPaymentCommand.Cancel command) {
        try {
            Long cancelAmount = command.amount() == null ? null : command.amount().longValue();
            return tossAdaptor.cancelPayment(command.paymentKey(), command.cancelReason(), cancelAmount);
        } catch (ExternalClientException e) {
            throw new GatewayRejectException(PaymentErrorCode.PAYMENT_TOSS_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new GatewayRejectException(PaymentErrorCode.PAYMENT_TOSS_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new PaymentUnknownStateException(PaymentErrorCode.PAYMENT_TOSS_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new PaymentUnknownStateException(PaymentErrorCode.PAYMENT_TOSS_UNAVAILABLE_ERROR, e.getErrorCode(), e.getMessage());
        }
    }

    public PGPaymentResult.Inquiry inquire(String paymentKey) {
        return null;
    }

}
