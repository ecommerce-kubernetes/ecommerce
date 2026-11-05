package com.example.userservice.auth.application.port.dto;

import com.example.userservice.common.domain.vo.Role;
import lombok.Builder;

@Builder
public record AuthUserResult(
        Long id,
        String email,
        String name,
        Role role
) {
}
