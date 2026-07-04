package com.example.order_service.cart.application.service.dto.result;

import com.example.order_service.cart.application.external.dto.result.CartProductStatus;
import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

@Builder
public record CartItemResult(
        Long cartItemId,
        CartProductStatus status,
        Long productId,
        Long productVariantId,
        String productName,
        String thumbnail,
        int quantity,
        CartItemPrice price,
        Money lineTotal,
        List<CartItemOption> options
) {
}
