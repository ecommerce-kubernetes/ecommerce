package com.example.userservice.api.user.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserOrderResponse {
    private Long userId;
    private Long pointBalance;
    private String userName;
    private String phoneNumber;
}
