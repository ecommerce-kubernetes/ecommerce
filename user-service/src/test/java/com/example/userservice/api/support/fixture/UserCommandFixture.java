package com.example.userservice.api.support.fixture;

import com.example.userservice.api.user.domain.model.Gender;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;

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
}
