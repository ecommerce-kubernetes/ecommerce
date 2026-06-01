package com.example.order_service.payment.application.external;

import com.example.order_service.infrastructure.adaptor.TossAdaptor;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentGateway {
    private final TossAdaptor tossAdaptor;

    public PgPaymentResult.Approval confirm(PGPaymentCommand.Confirm command) {
        return null;
    }
}
