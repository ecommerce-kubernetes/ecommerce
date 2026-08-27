package com.example.order_service.infrastructure.dto.response.user;

import lombok.Builder;

@Builder
public record UserProfileResponse(
        Long userId,
        String userName,
        String phoneNumber,
        Long availablePoints,
        ShippingAddressResponse defaultShippingAddress
) {

    @Builder
    public record ShippingAddressResponse(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {}
}
