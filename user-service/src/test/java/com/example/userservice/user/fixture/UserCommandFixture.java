package com.example.userservice.user.fixture;

import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.domain.vo.Gender;

import java.time.LocalDate;

public class UserCommandFixture {

    public static UserCreateCommand.UserCreateCommandBuilder anUserCreateCommand() {
        return UserCreateCommand.builder()
                .email("la9814@naver.com")
                .password("password1234*")
                .name("김이박")
                .birthDate(LocalDate.of(1999, 12, 25))
                .gender(Gender.MALE)
                .phoneNumber("010-1234-5678");
    }

    public static AddShippingAddressCommand.AddShippingAddressCommandBuilder anAddShippingAddressCommand() {
        return AddShippingAddressCommand.builder()
                .userId(1L)
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호");
    }
}
