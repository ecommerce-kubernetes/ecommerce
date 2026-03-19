package com.example.userservice.api.auth.service.dto;

import com.example.userservice.api.user.domain.model.Role;
import com.example.userservice.api.user.domain.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtClaims {
    private Long id;
    private String email;
    private String name;
    private Role role;

    public static JwtClaims of(User user) {
        return JwtClaims.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
