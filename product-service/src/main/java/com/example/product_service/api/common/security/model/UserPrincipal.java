package com.example.product_service.api.common.security.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserPrincipal {
    private Long userId;
    private UserRole userRole;

    @Builder
    private UserPrincipal(Long userId, UserRole userRole) {
        this.userId = userId;
        this.userRole = userRole;
    }

    public static UserPrincipal of(Long userId, UserRole userRole) {
        return UserPrincipal.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
    }
}
