package com.example.product_service.api.support;

import com.example.product_service.api.common.security.model.UserPrincipal;
import com.example.product_service.api.common.security.model.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    @GetMapping("/security")
    public String securityGetMapping(@AuthenticationPrincipal UserPrincipal userPrincipal) throws JsonProcessingException {
        Long userId = userPrincipal.getUserId();
        UserRole userRole = userPrincipal.getUserRole();
        return "userId=" + userId + ",userRole="+userRole.name();
    }

    @PostMapping("/test")
    public TestRequest trimTest(@RequestBody TestRequest request){
        return request;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class TestRequest {
        private String text;
    }
}
