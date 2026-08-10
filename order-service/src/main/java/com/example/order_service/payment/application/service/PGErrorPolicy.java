package com.example.order_service.payment.application.service;

import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class PGErrorPolicy {

    private static final Set<PaymentPGPortErrorCode> ABORT_TARGETS = EnumSet.of(
            PaymentPGPortErrorCode.PG_INSUFFICIENT_BALANCE,
            PaymentPGPortErrorCode.PG_METHOD_REJECTED,
            PaymentPGPortErrorCode.PG_INVALID_REQUEST,
            PaymentPGPortErrorCode.PG_NOT_FOUND,
            PaymentPGPortErrorCode.PG_AUTH_ERROR,
            PaymentPGPortErrorCode.UNSUPPORTED_PROVIDER,
            PaymentPGPortErrorCode.PG_CIRCUIT_OPEN
    );

    public boolean isAbortTargetOnApprove(PaymentPGPortErrorCode errorCode) {
        return ABORT_TARGETS.contains(errorCode);
    }
}
