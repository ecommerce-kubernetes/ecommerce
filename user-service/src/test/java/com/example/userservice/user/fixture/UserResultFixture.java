package com.example.userservice.user.fixture;

import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserCreateResult;
import com.example.userservice.user.application.service.dto.result.UserBalanceResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;

public class UserResultFixture {

    public static UserCreateResult.UserCreateResultBuilder anUserCreateResult() {
        return UserCreateResult.builder()
                .userId(1L);
    }

    public static EmailAvailableResult.EmailAvailableResultBuilder anEmailAvailableResult() {
        return EmailAvailableResult.builder()
                .available(true);
    }

    public static UserProfileResult.UserProfileResultBuilder anUserProfileResult() {
        UserProfileResult.ShippingAddressResult defaultShippingAddress = UserProfileResult.ShippingAddressResult.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();

        return UserProfileResult.builder()
                .userId(1L)
                .userName("김이박")
                .phoneNumber("010-1234-5678")
                .availablePoints(10000L)
                .defaultShippingAddress(defaultShippingAddress);
    }

    public static UserBalanceResult.UserBalanceResultBuilder anUserPointsResult() {
        return UserBalanceResult.builder()
                .userId(1L)
                .availablePoints(10000L);
    }
}
