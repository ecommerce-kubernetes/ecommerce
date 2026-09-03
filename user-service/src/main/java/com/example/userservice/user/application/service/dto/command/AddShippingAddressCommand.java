package com.example.userservice.user.application.service.dto.command;

import lombok.Builder;

@Builder
public record AddShippingAddressCommand(
        Long userId,
        String receiverName,
        String receiverPhone,
        String zipCode,
        String address,
        String addressDetail,
        boolean isDefault
) {
}
