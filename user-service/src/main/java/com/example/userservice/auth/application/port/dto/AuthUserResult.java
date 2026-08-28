package com.example.userservice.auth.application.port.dto;

import com.example.userservice.user.domain.model.Role;
import lombok.Builder;

@Builder
public record AuthUserResult(
        Long id,
        String email,
        String name,
        String encryptedPwd,
        Role role
) {
}
