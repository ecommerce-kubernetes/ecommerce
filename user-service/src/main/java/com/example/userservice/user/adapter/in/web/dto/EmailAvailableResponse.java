package com.example.userservice.user.adapter.in.web.dto;

import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailAvailableResponse {
    private boolean available;

    public static EmailAvailableResponse from(EmailAvailableResult result) {
        return EmailAvailableResponse.builder()
                .available(result.available())
                .build();
    }
}
