package com.example.userservice.auth.fixture;

import com.example.userservice.api.user.domain.model.Role;
import com.example.userservice.auth.application.port.dto.AuthUserResult;

public class AuthUserResultFixture {

    public static AuthUserResult.AuthUserResultBuilder anAuthUserResult() {
        return AuthUserResult.builder()
                .id(1L)
                .email("la9814@naver.com")
                .name("민식")
                .encryptedPwd("encryptedPassword")
                .role(Role.ROLE_USER);
    }
}
