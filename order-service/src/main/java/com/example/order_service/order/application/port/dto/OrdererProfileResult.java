package com.example.order_service.order.application.port.dto;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;

@Builder
public record OrdererProfileResult(
        Orderer orderer,
        Money availablePoints,
        ShippingAddress defaultShippingAddress
) {
}
