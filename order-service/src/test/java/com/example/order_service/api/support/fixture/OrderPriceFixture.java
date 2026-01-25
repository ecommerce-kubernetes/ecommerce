package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;

public class OrderPriceFixture {

    public static OrderProductAmount.OrderProductAmountBuilder anOrderProductAmount() {
        return OrderProductAmount.builder()
                .totalOriginalAmount(10000L)
                .totalDiscountAmount(1000L)
                .subTotalAmount(9000L);
    }
}
