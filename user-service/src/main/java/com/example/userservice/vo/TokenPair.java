package com.example.userservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenPair {
    private String accessToken;
    private String refreshToken;
}
