package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

public class UserClientResponse {

    @Builder
    public record Profile(
            Long userId,
            String userName,
            String phoneNumber,
            ShippingAddress defaultShippingAddress
    ) {
    }

    @Builder
    public record UserPoints(
            Long userId,
            Long ownedPoints
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
