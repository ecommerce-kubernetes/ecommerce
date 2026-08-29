package com.example.userservice.user.adapter.in.web.dto;

import com.example.userservice.user.application.service.dto.result.UserCreateResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record UserCreateResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long userId
) {
    public static UserCreateResponse from(UserCreateResult result) {
        return UserCreateResponse.builder()
                .userId(result.userId())
                .build();
    }
}
