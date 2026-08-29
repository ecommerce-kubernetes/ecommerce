package com.example.userservice.user.fixture;

import com.example.userservice.user.adapter.in.web.dto.AddShippingAddressRequest;
import com.example.userservice.user.adapter.in.web.dto.UserCreateRequest;

import java.time.LocalDate;

public class UserRequestFixture {

    public static UserCreateRequest.UserCreateRequestBuilder anUserCreateRequest() {
        return UserCreateRequest.builder()
                .email("la9814@naver.com")
                .password("password1234*")
                .name("김이박")
                .birthDate(LocalDate.of(1999, 12, 25))
                .gender("MALE")
                .phoneNumber("010-1234-5678");
    }

    public static AddShippingAddressRequest.AddShippingAddressRequestBuilder anAddShippingAddressRequest() {
        return AddShippingAddressRequest.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .isDefault(false);
    }
}
