package com.example.userservice.api.common.error.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String timestamp;
    private String path;

    public static ErrorResponse of(String code, String message, String timestamp, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(timestamp)
                .path(path)
                .build();
    }
}
