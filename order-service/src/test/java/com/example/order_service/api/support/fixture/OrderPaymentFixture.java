package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.service.dto.result.OrderPaymentInfo;

import java.time.LocalDateTime;

public class OrderPaymentFixture {

    public static final String ORDER_NO = "ORD-20260101-adsvc";

    public static OrderPaymentInfo.OrderPaymentInfoBuilder anOrderPaymentInfo() {
        return OrderPaymentInfo.builder()
                .orderNo(ORDER_NO)
                .paymentKey("paymentKey")
                .totalAmount(7000L)
                .status("DONE")
                .method("CARD")
                .approvedAt(LocalDateTime.now().toString());
    }
}
