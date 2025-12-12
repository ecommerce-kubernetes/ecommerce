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

    public static ErrorResponse toNotFound(String message, String timestamp, String path){
        return of(HttpStatus.NOT_FOUND.name(), message, timestamp, path);
    }

    public static ErrorResponse toUnavailableServer(String message, String timestamp, String path){
        return of(HttpStatus.SERVICE_UNAVAILABLE.name(), message, timestamp, path);
    }

    public static ErrorResponse toInternalServerError(String message, String timestamp, String path){
        return of(HttpStatus.INTERNAL_SERVER_ERROR.name(), message, timestamp, path);
    }

    public static ErrorResponse toNoPermission(String message, String timestamp, String path){
        return of(HttpStatus.FORBIDDEN.name(), message, timestamp, path);
    }

    public static ErrorResponse toUnAuthorized(String message, String timestamp, String path){
        return of(HttpStatus.UNAUTHORIZED.name(), message, timestamp, path);
    }

    public static ErrorResponse toConflict(String message, String timestamp, String path){
        return of(HttpStatus.CONFLICT.name(), message, timestamp, path);
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