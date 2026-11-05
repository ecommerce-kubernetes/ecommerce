package com.example.order_service.payment.application.service.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.service.dto.command.PaymentCancelCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.domain.PaymentProvider;

public class PaymentCommandFixture {

    public static PaymentCreateCommand.PaymentCreateCommandBuilder anCreateCommand() {
        return PaymentCreateCommand.builder()
                .userId(1L)
                .orderId(1L);
    }

    public static PaymentConfirmCommand.PaymentConfirmCommandBuilder anConfirmCommand() {
        return PaymentConfirmCommand.builder()
                .paymentId(1L)
                .userId(1L)
                .paymentKey("paymentKey")
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS);
    }

    public static PaymentCancelCommand.PaymentCancelCommandBuilder anCancelCommand() {
        return PaymentCancelCommand.builder()
                .orderId(1L)
                .userId(1L)
                .cancelReason("주문 취소");
    }
}
