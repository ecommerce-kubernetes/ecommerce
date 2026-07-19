package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;

@Deprecated
public class OrderUserResult {

    @Builder
    public record Profile(
            Orderer orderer,
            Money availablePoints,
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
