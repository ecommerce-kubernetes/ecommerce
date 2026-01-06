package com.example.order_service.api.common.client.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossErrorResponse {
    private String code;
    private String message;

    @Builder
    public TossErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
