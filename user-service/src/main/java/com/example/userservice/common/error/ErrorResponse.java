package com.example.userservice.common.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ErrorResponse(

        String code,

        String message,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<InputError> errors,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime timestamp,

        String path
) {

    public static ErrorResponse of(String code, String message, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    public static ErrorResponse ofValidation(List<InputError> errors, String path) {
        return ErrorResponse.builder()
                .code("INVALID_INPUT_VALUE")
                .message("입력값이 올바르지 않습니다.")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    public record InputError(String field, String reason) {
        public static InputError of(String field, String reason) {
            return new InputError(field, reason);
        }
    }
}
