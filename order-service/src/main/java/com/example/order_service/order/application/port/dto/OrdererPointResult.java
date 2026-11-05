package com.example.order_service.order.application.port.dto;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Builder
public record OrdererPointResult(
        Long userId,
        Money availablePoints
) {
}
