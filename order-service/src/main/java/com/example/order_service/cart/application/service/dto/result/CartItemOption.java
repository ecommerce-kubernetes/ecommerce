package com.example.order_service.cart.application.service.dto.result;

import lombok.Builder;

@Builder
public record CartItemOption(
        String optionTypeName,
        String optionValueName
) {
}
