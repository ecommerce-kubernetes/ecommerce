package com.example.userservice.user.application.service.dto.result;

import com.example.userservice.user.domain.vo.Role;
import lombok.Builder;

@Builder
public record UserIdentityResult(
        Long userId,
        Role role
) {
}
