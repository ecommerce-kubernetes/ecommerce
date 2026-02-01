package com.example.userservice.api.auth.controller;

import com.example.userservice.api.auth.controller.dto.LoginRequest;
import com.example.userservice.api.auth.service.AuthService;
import com.example.userservice.api.auth.service.dto.LoginResponse;
import com.example.userservice.api.auth.service.dto.TokenData;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private ResponseCookie setRefreshTokenCookie(String refreshToken){
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("None")
                .build();
    }

}
