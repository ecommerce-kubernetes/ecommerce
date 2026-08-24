package com.example.order_service.payment.application.service.fixture;

import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;

public class PaymentCommandFixture {

    public static PaymentCreateCommand.PaymentCreateCommandBuilder anCreateCommand() {
        return PaymentCreateCommand.builder()
                .userId(1L)
                .orderId(1L)
                .build();
    }
}
