package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;

@Builder
public record OrdererProfileResult(
        Orderer orderer,
        ShippingAddress defaultShippingAddress
) {
}
