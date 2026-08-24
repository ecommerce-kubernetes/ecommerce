package com.example.order_service.payment.application.service.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderStatus;

public class PaymentOrderResultFixture {

    public static PaymentOrderResult.PaymentOrderResultBuilder anPaymentOrder() {
        return PaymentOrderResult.builder()
                .orderId(1L)
                .status(PaymentOrderStatus.PENDING)
                .orderName("상품")
                .totalAmount(Money.wons(1000L));
    }
}
