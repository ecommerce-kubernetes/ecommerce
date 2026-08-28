package com.example.userservice.auth.fixture;

import com.example.userservice.auth.service.dto.LoginResponse;
import com.example.userservice.auth.service.dto.TokenData;

public class AuthResponseFixture {

    public static TokenData.TokenDataBuilder anTokenData() {
        return TokenData.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken");
    }
}
