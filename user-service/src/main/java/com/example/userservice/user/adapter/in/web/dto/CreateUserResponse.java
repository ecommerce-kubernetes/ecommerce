package com.example.userservice.user.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record CreateUserResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long userId
) {
    public static CreateUserResponse from(Long userId) {
        return CreateUserResponse.builder()
                .userId(userId)
                .build();
    }
}
