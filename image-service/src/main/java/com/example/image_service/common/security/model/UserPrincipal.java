package com.example.image_service.common.security.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPrincipal {
    private Long userId;
    private UserRole userRole;

    public static UserPrincipal of(Long userId, UserRole userRole) {
        return UserPrincipal.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
    }
}
