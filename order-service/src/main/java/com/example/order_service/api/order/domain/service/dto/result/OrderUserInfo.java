package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderUserInfo {
    private Long userId;
    private String userName;
    private String phoneNumber;
}
