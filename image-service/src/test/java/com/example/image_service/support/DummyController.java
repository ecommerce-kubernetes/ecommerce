package com.example.image_service.support;

import com.example.image_service.common.security.model.UserPrincipal;
import com.example.image_service.common.security.model.UserRole;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    @GetMapping("/security")
    public String securityGetMapping(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getUserId();
        UserRole userRole = userPrincipal.getUserRole();
        return "userId=" + userId + ",userRole="+userRole.name();
    }

    @GetMapping("/security/permission")
    @PreAuthorize("hasRole('ADMIN')")
    public String permissionError() {
        return "ok";
    }
}
