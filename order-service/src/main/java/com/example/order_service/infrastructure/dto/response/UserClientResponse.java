package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

@Deprecated
public class UserClientResponse {

    @Builder
    public record Profile(
            Long userId,
            String userName,
            String phoneNumber,
            ShippingInfo defaultShippingInfo
    ) {
    }

    @Builder
    public record UserPoints(
            Long userId,
            Long ownedPoints
    ) {
    }

    @Builder
    public record ShippingInfo(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
    }

}
