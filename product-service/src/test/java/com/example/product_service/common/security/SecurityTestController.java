package com.example.product_service.common.security;

import com.example.product_service.common.security.model.UserPrincipal;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@TestComponent
@RestController
public class SecurityTestController {

    @GetMapping("/categories")
    public String category() {
        return "ok";
    }

    @GetMapping("/admin")
    public String admin() {
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
