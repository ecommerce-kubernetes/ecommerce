package com.example.userservice.auth.adapter.in.web;

import com.example.userservice.auth.adapter.in.web.dto.LoginRequest;
import com.example.userservice.auth.application.service.AuthService;
import com.example.userservice.auth.adapter.in.web.dto.TokenResponse;
import com.example.userservice.common.security.model.UserPrincipal;
import com.example.userservice.auth.application.service.dto.TokenResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Validated @RequestBody LoginRequest request) {
        TokenResult token = authService.login(request.email(), request.password());
        TokenResponse response = TokenResponse.of(token.accessToken());
        ResponseCookie refreshTokenCookie = setRefreshTokenCookie(token.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = "refreshToken") @NotBlank(message = "{refresh.token.notBlank}") String refreshToken
    ) {
        TokenResult token = authService.refresh(refreshToken);
        TokenResponse response = TokenResponse.of(token.accessToken());
        ResponseCookie refreshTokenCookie = setRefreshTokenCookie(token.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        authService.logout(userPrincipal.getUserId());
        ResponseCookie responseCookie = deleteCookie();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .build();
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

    private ResponseCookie deleteCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
    }

}
