package com.example.order_service.api.support.security;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class DummyController {

    @GetMapping("/security")
    public String securityGetMapping(@AuthenticationPrincipal UserPrincipal userPrincipal) throws JsonProcessingException {
        Long userId = userPrincipal.getUserId();
        UserRole userRole = userPrincipal.getUserRole();
        return "userId=" + userId + ",userRole="+userRole.name();
    }
}
