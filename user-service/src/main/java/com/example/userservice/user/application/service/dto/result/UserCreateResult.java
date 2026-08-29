package com.example.userservice.user.application.service.dto.result;

import com.example.userservice.user.domain.User;
import lombok.Builder;

@Builder
public record UserCreateResult(
        Long userId
) {
    public static UserCreateResult from(User user) {
        return UserCreateResult.builder()
                .userId(user.getId())
                .build();
    }
}
