package com.example.order_service.infrastructure.dto.response;


public record TossErrorResponse(
        String code,
        String message
) {
}
