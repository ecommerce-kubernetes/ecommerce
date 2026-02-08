package com.example.userservice.api.support.fixture;

import com.example.userservice.api.auth.service.dto.LoginResponse;
import com.example.userservice.api.auth.service.dto.TokenData;

public class AuthResponseFixture {

    public static LoginResponse.LoginResponseBuilder anLoginResponse() {
        return LoginResponse.builder()
                .accessToken("accessToken");
    }

    public static TokenData.TokenDataBuilder anTokenData() {
        return TokenData.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken");
    }
}
