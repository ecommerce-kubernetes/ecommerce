package com.example.order_service.order.application.service.ordersheet.dto.command;

import lombok.Builder;

@Builder
public record UpdateOrderSheetShippingAddressCommand(
        String orderSheetId,
        Long userId,
        String receiverName,
        String receiverPhone,
        String zipCode,
        String address,
        String addressDetail
) {
}
