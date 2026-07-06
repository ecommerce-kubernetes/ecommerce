package com.example.order_service.cart.application.dto.result;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

@Builder
public record CartItemResult(
        Long cartItemId,
        CartItemAvailability status,
        Long productId,
        Long productVariantId,
        String productName,
        String thumbnail,
        int quantity,
        CartItemPrice price,
        Money lineTotal,
        List<CartItemOption> options
) {

    public static CartItemResult from(CartItemData data, CartProductResult product, CartItemAvailability status) {
        return CartItemResult.builder()
                .cartItemId(data.cartItemId())
                .status(status)
                .productId(product.productId())
                .productVariantId(product.productVariantId())
                .productName(product.productName())
                .thumbnail(product.thumbnail())
                .quantity(data.quantity())
                .price(CartItemPrice.from(product))
                .lineTotal(product.discountedPrice().multiple(data.quantity()))
                .options(CartItemOption.from(product.options()))
                .build();
    }

    public static CartItemResult unknown(CartItemData data, CartItemAvailability status) {
        return CartItemResult.builder()
                .cartItemId(data.cartItemId())
                .productVariantId(data.productVariantId())
                .status(status)
                .quantity(data.quantity())
                .build();
    }
}
