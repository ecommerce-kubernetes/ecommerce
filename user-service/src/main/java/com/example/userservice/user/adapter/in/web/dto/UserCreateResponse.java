package com.example.userservice.user.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record UserCreateResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long userId
) {
    public static UserCreateResponse from(Long userId) {
        return UserCreateResponse.builder()
                .userId(userId)
                .build();
    }
}
