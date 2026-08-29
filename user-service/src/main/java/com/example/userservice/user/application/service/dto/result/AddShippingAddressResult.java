package com.example.userservice.user.application.service.dto.result;

import lombok.Builder;

@Builder
public record AddShippingAddressResult(
        Long userId
) {
    public static AddShippingAddressResult of(Long userId) {
        return AddShippingAddressResult.builder()
                .userId(userId)
                .build();
    }
}
