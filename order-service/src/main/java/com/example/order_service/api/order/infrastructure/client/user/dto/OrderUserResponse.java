package com.example.order_service.api.order.infrastructure.client.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderUserResponse {
    private Long userId;
    private Long pointBalance;

    @Builder
    private OrderUserResponse(Long userId, Long pointBalance){
        this.userId = userId;
        this.pointBalance = pointBalance;
    }
}
