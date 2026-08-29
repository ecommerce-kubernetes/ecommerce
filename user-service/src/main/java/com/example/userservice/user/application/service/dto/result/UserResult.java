package com.example.userservice.user.application.service.dto.result;

import com.example.userservice.user.domain.vo.Role;
import lombok.Builder;

@Builder
public record UserResult(
        Long userId,
        String email,
        String phoneNumber,

        String name,
        Role role
) {
}
