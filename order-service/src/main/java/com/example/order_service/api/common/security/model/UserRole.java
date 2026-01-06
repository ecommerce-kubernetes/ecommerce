package com.example.order_service.api.common.security.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ROLE_ADMIN("관리자"),
    ROLE_USER("사용자");
    private final String text;
}
