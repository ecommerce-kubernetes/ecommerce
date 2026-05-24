package com.example.order_service.order.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

public class OrderUserResult {

    @Builder
    public record UserPoint(
            Long userId,
            Money ownedPoints,
            Money availablePoints
    ) {}

    @Builder
    public record OrdererInfo(
            Long userId,
            Long availablePoints,
            String ordererName,
            String ordererPhone
    ) {
    }
}
