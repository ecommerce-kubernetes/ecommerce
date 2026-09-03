package com.example.userservice.user.application.service.dto.result;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.user.domain.User;
import lombok.Builder;

@Builder
public record UserBalanceResult(
        Long userId,
        Money availablePoints
) {
    public static UserBalanceResult from(User user) {
        return UserBalanceResult.builder()
                .userId(user.getId())
                .availablePoints(user.getPoint())
                .build();
    }
}
