package com.example.order_service.order.adapter.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.order.domain.vo.ProductSnapshot;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class OrderResultFixture {
    public static OrderSummaryResult.OrderSummaryResultBuilder anSummaryResult() {
        OrderSummaryResult.OrderItemResult item = anSummaryItemResult().build();
        return OrderSummaryResult.builder()
                .orderId(1L)
                .status(OrderStatus.PENDING)
                .orderItems(List.of(item))
                .createdAt(LocalDateTime.now());
    }

    public static OrderSummaryResult.OrderItemResult.OrderItemResultBuilder anSummaryItemResult() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(1000L), Money.ZERO, Money.wons(1000L), Money.ZERO, Money.wons(1000L));
        return OrderSummaryResult.OrderItemResult.builder()
                .orderItemId(1L)
                .product(productSnapshot)
                .options(Collections.emptyList())
                .quantity(1)
                .orderItemAmount(orderItemAmount);
    }
}
