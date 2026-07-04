package com.example.order_service.cart.application.service.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Builder
public record CartItemPrice(
        Money originalPrice,
        long discountRate,
        Money discountAmount,
        Money discountedPrice
) {
}
