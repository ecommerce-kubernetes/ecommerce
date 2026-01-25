package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;

import java.time.LocalDateTime;

public class OrderResponseFixture {

    public static CreateOrderResponse.CreateOrderResponseBuilder anCreateOrderResponse(String orderNo) {
        return CreateOrderResponse.builder()
                .orderNo(orderNo)
                .status(OrderStatus.PENDING.name())
                .orderName("상품")
                .finalPaymentAmount(7000L)
                .createdAt(LocalDateTime.now().toString());
    }
}
