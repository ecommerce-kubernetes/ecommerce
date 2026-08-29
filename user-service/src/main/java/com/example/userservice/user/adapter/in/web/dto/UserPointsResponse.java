package com.example.userservice.user.adapter.in.web.dto;

import com.example.userservice.user.application.service.dto.result.UserPointsResult;
import lombok.Builder;

@Builder
public record UserPointsResponse(
        Long userId,
        Long availablePoints
) {
    public static UserPointsResponse from(UserPointsResult result) {
        return UserPointsResponse.builder()
                .userId(result.userId())
                .availablePoints(result.availablePoints())
                .build();
    }
}
