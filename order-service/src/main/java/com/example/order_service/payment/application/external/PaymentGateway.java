package com.example.order_service.payment.application.external;

import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.infrastructure.gateway.TossGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgErrorTranslator;
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
    private final PgErrorTranslator errorTranslator;

    public PGPaymentResult.Approval confirm(PGPaymentCommand.Confirm command) {
        return null;
    }

    public PGPaymentResult.Cancellation cancel(PGPaymentCommand.Cancel command) {
        return null;
    }

    public PGPaymentResult.Inquiry inquire(String paymentKey) {
        return null;
    }
}
