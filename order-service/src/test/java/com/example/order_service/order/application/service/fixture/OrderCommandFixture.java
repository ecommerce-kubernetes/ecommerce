package com.example.order_service.order.application.service.fixture;

import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;

public class OrderCommandFixture {

    public static CreateOrderCommand.CreateOrderCommandBuilder anCreateOrderCommand() {
        return CreateOrderCommand.builder()
                .userId(1L)
                .orderSheetId(1L);

    }
}
