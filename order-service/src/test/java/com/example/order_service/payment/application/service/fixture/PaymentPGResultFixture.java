package com.example.order_service.payment.application.service.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentPGStatus;
import com.example.order_service.payment.domain.PaymentMethod;

import java.time.LocalDateTime;

public class PaymentPGResultFixture {
    public static PGConfirmResult.PGConfirmResultBuilder anPGConfirmResult() {
        return PGConfirmResult.builder()
                .status(PaymentPGStatus.DONE)
                .amount(Money.wons(1000L))
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .approvedAt(LocalDateTime.now());
    }
}
