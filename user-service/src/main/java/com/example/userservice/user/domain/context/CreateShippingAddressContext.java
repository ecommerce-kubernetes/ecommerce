package com.example.userservice.user.domain.context;

import lombok.Builder;

@Builder
public record CreateShippingAddressContext(
        String receiverName,
        String receiverPhone,
        String zipCode,
        String address,
        String addressDetail,
        boolean isDefault
) {
}
