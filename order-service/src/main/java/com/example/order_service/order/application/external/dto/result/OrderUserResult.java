package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;

public class OrderUserResult {

    @Builder
    public record Profile(
            Long userId,
            String userName,
            String phoneNumber,
            ShippingAddress shippingAddress
    ) {
    }

    @Builder
    public record UserPoint(
            Long userId,
            Money ownedPoints
    ) {
    }
}
