package com.example.userservice.user.application.service.dto.result;

import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.vo.Role;
import lombok.Builder;

@Builder
public record UserIdentityResult(
        Long userId,
        String email,
        String name,
        Role role
) {
    public static UserIdentityResult from(User user) {
        return UserIdentityResult.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
