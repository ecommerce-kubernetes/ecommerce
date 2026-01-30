package com.example.order_service.api.support.fixture.order;

import com.example.order_service.api.order.domain.model.PaymentMethod;
import com.example.order_service.api.order.domain.model.PaymentStatus;
import com.example.order_service.api.order.domain.service.dto.result.OrderPaymentInfo;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;

import java.time.LocalDateTime;

public class OrderPaymentFixture {

    public static final String ORDER_NO = "ORD-20260101-adsvc";

    public static OrderPaymentInfo.OrderPaymentInfoBuilder anOrderPaymentInfo() {
        return OrderPaymentInfo.builder()
                .orderNo(ORDER_NO)
                .paymentKey("paymentKey")
                .totalAmount(7000L)
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .approvedAt(LocalDateTime.now());
    }

    public static TossPaymentConfirmResponse.TossPaymentConfirmResponseBuilder anTossPaymentResponse() {
        return TossPaymentConfirmResponse.builder()
                .orderId(ORDER_NO)
                .paymentKey("paymentKey")
                .totalAmount(7000L)
                .status("DONE")
                .method("CARD")
                .approvedAt(LocalDateTime.now().toString());
    }
}
