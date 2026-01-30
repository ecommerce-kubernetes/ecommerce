package com.example.order_service.api.support.fixture.order;

import com.example.order_service.api.order.domain.service.dto.result.CalculatedOrderAmounts;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;

public class OrderPriceFixture {

    public static OrderProductAmount.OrderProductAmountBuilder anOrderProductAmount() {
        return OrderProductAmount.builder()
                .totalOriginalAmount(10000L)
                .totalDiscountAmount(1000L)
                .subTotalAmount(9000L);
    }

    public static CalculatedOrderAmounts.CalculatedOrderAmountsBuilder anCalculatedOrderAmounts() {
        return CalculatedOrderAmounts.builder()
                .totalOriginalAmount(10000L)
                .totalProductDiscount(1000L)
                .couponDiscountAmount(1000L)
                .usePointAmount(1000L)
                .finalPaymentAmount(7000L);
    }
}
