package com.example.order_service.api.common.error.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private String timestamp;
    private String path;

    @Builder
    public ErrorResponse(String code, String message, String timestamp, String path) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
    }

    public static ErrorResponse of(String code, String message, String timestamp, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(timestamp)
                .path(path)
                .build();
    }
}