package com.example.userservice.api.common.security.model;

import com.example.userservice.api.user.domain.model.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserPrincipal {
    private Long userId;
    private Role userRole;

    @Builder
    private UserPrincipal(Long userId, Role userRole) {
        this.userId = userId;
        this.userRole = userRole;
    }

    public static UserPrincipal of(Long userId, Role userRole) {
        return UserPrincipal.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
    }
}
