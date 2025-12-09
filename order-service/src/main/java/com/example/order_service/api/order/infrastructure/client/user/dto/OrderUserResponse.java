package com.example.order_service.api.order.infrastructure.client.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderUserResponse {
    private Long userId;
    private Long pointBalance;

    @Builder
    private OrderUserResponse(Long userId, Long pointBalance){
        this.userId = userId;
        this.pointBalance = pointBalance;
    }
}
