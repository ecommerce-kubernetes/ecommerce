package com.example.userservice.api.auth.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenData {
    private String accessToken;
    private String refreshToken;
}
