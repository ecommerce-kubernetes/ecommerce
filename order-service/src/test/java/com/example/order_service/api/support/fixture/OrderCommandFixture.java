package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;

import java.util.List;

public class OrderCommandFixture {

    public static CreateOrderCommand.CreateOrderCommandBuilder anOrderCommand() {
        return CreateOrderCommand.builder()
                .userId(1L)
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(1000L)
                .expectedPrice(9000L)
                .orderItemCommands(List.of(anOrderItemCommand().build()));
    }

    public static CreateOrderItemCommand.CreateOrderItemCommandBuilder anOrderItemCommand() {
        return CreateOrderItemCommand.builder()
                .productVariantId(1L)
                .quantity(1);
    }

    public static OrderSearchCondition.OrderSearchConditionBuilder anOrderSearchCondition() {
        return OrderSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("latest");
    }
}
