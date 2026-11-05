package com.example.order_service.cart.application.service.dto.result;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Builder
public record CartItemPrice(
        Money originalPrice,
        long discountRate,
        Money discountAmount,
        Money discountedPrice
) {
    public static CartItemPrice from(CartProductResult.CartProductDetail result) {
        return CartItemPrice.builder()
                .originalPrice(result.originalPrice())
                .discountRate(result.discountRate())
                .discountAmount(result.discountAmount())
                .discountedPrice(result.discountedPrice())
                .build();
    }
}
