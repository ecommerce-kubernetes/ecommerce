package com.example.userservice.api.auth.controller;

import com.example.userservice.api.auth.controller.dto.LoginRequest;
import com.example.userservice.api.auth.service.AuthService;
import com.example.userservice.api.auth.service.dto.LoginResponse;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.common.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        TokenData token = authService.login(request.getEmail(), request.getPassword());
        LoginResponse response = LoginResponse.of(token.getAccessToken());
        ResponseCookie refreshTokenCookie = setRefreshTokenCookie(token.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_MISSING);
        }
        TokenData token = authService.refresh(refreshToken);
        LoginResponse response = LoginResponse.of(token.getAccessToken());
        ResponseCookie refreshTokenCookie = setRefreshTokenCookie(token.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    private ResponseCookie setRefreshTokenCookie(String refreshToken){
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("None")
                .build();
    }

}
