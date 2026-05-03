package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

public class UserClientResponse {

    @Builder
    public record OrderUserInfo(
            Long userId,
            Long pointBalance,
            String userName,
            String phoneNumber
    ) {}
}
