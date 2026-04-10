package com.example.userservice.api.support;

import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import com.example.userservice.api.common.security.model.UserPrincipal;
import com.example.userservice.api.user.domain.model.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class DummyController {

    @GetMapping("/exception")
    public String throwBusinessException() {
        throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }

    @PostMapping("/not-readable")
    public String throwNotReadableException(@RequestBody TestRequestBody requestBody) {
        return "OK";
    }

    @GetMapping("/security")
    public String securityGetMapping(@AuthenticationPrincipal UserPrincipal userPrincipal) throws JsonProcessingException {
        Long userId = userPrincipal.getUserId();
        Role userRole = userPrincipal.getUserRole();
        return "userId=" + userId + ",userRole="+userRole.name();
    }

    @GetMapping("/security/permission")
    @PreAuthorize("hasRole('ADMIN')")
    public String permissionError() {
        return "ok";
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class TestRequestBody {
        private LocalDateTime datetime;
        private Long number;
    }
}
