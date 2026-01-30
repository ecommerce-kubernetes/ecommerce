package com.example.order_service.api.support.fixture.order;

import com.example.order_service.api.order.domain.service.dto.result.OrderUserInfo;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;

public class OrderUserFixture {
    public static OrderUserResponse.OrderUserResponseBuilder anOrderUserResponse(){
        return OrderUserResponse.builder()
                .userId(1L)
                .userName("유저")
                .phoneNumber("010-1234-5678")
                .pointBalance(1000L);
    }

    public static OrderUserInfo.OrderUserInfoBuilder anOrderUserInfo() {
        return OrderUserInfo.builder()
                .userId(1L)
                .userName("유저")
                .phoneNumber("010-1234-5678");
    }
}
