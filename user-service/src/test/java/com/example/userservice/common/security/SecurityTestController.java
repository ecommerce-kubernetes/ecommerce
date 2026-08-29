package com.example.userservice.common.security;

import com.example.userservice.common.security.model.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {

    @PostMapping("/users")
    public String createUser() {
        return "ok";
    }

    @GetMapping("/users/email-availability")
    public String checkEmail() {
        return "ok";
    }

    @PostMapping("/auth/login")
    public String login() {
        return "ok";
    }

    @PostMapping("/auth/refresh")
    public String refresh() {
        return "ok";
    }

    @PostMapping("/users/shipping-addresses")
    public String addAddress() {
        return "ok";
    }

    @DeleteMapping("/users/shipping-addresses/{id}")
    public String deleteAddress() {
        return "ok";
    }

    @PostMapping("/auth/logout")
    public String logout() {
        return "ok";
    }

    @GetMapping("/security")
    public String securityGetMapping(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return "ok";
    }

    @GetMapping("/security/permission")
    @PreAuthorize("hasRole('ADMIN')")
    public String permissionError() {
        return "ok";
    }
}
