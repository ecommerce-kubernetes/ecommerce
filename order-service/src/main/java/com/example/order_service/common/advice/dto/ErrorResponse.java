package com.example.order_service.common.advice.dto;

import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private String timestamp;
    private String path;

    @Builder
    public ErrorResponse(String error, String message, String timestamp, String path) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
    }

    public static ErrorResponse toBadRequest(String message, String timestamp, String path) {
        return of(HttpStatus.BAD_REQUEST.name(), message, timestamp, path);
    }

    public static ErrorResponse of(String error, String message, String timestamp, String path) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .timestamp(timestamp)
                .path(path)
                .build();
    }
}