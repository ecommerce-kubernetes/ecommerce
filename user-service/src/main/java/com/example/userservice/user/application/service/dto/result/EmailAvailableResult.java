package com.example.userservice.user.application.service.dto.result;

import lombok.Builder;

@Builder
public record EmailAvailableResult(
        boolean available
) {
    public static EmailAvailableResult of(boolean available) {
        return EmailAvailableResult.builder()
                .available(available)
                .build();
    }
}
