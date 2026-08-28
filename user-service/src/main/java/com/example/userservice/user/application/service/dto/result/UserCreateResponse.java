package com.example.userservice.user.application.service.dto.result;

import com.example.userservice.user.domain.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCreateResponse {
    private Long id;

    public static UserCreateResponse from(User user) {
        return UserCreateResponse.builder()
                .id(user.getId())
                .build();
    }
}
