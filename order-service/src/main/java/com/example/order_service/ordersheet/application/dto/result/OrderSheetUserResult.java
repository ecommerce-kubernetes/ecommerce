package com.example.order_service.ordersheet.application.dto.result;

import lombok.Builder;

public class OrderSheetUserResult {

    @Builder
    public record Profile(
            Long userId,
            String userName,
            String phoneNumber,
            ShippingAddress shippingAddress
    ) {
    }

    @Builder
    public record ShippingAddress(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
    }
}
