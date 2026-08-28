package com.example.userservice.api.common.error.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ValidationErrorResponse(
        String code,
        String message,
        List<FieldDetail> errors,
        LocalDateTime timestamp,
        String path
) {

    public record FieldDetail(
            String field,
            String reason
    ) {

        public static FieldDetail of(String field, String reason) {
            return new FieldDetail(field, reason);
        }
    }
}
