package com.example.userservice.user.application.service.dto.result;

import lombok.Builder;

@Builder
public record UserProfileResult(
        Long userId,
        String userName,
        String phoneNumber,
        Long availablePoints,
        ShippingAddressResult defaultShippingAddress
) {

    @Builder
    public record ShippingAddressResult(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
    }
}
