package com.example.userservice.api.user.service.dto.result;

import com.example.userservice.api.user.domain.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserOrderResponse {
    private Long userId;
    private Long pointBalance;
    private String userName;
    private String phoneNumber;

    public static UserOrderResponse from(User user) {
        return UserOrderResponse.builder()
                .userId(user.getId())
                .pointBalance(user.getPoint())
                .userName(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
