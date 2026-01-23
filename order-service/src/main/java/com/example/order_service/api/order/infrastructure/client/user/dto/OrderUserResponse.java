package com.example.order_service.api.order.infrastructure.client.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderUserResponse {
    private Long userId;
    private Long pointBalance;
    private String userName;
    private String phoneNumber;

    public boolean hasEnoughPoints(long pointToUse){
        return this.pointBalance >= pointToUse;
    }
}
