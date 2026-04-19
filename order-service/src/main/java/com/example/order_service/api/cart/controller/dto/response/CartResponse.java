package com.example.order_service.api.cart.controller.dto.response;

import com.example.order_service.api.cart.facade.dto.result.CartItemStatus;
import com.example.order_service.api.cart.facade.dto.result.CartResult;
import lombok.Builder;

import java.util.List;

public class CartResponse {

    @Builder
    public record Cart(
            List<Detail> items,
            long totalOriginalPrice,
            long totalDiscountAmount,
            long totalFinalPrice
    ) {
        public static Cart from(CartResult.Cart result) {
            return Cart.builder()
                    .items(Detail.from(result.items()))
                    .totalOriginalPrice(result.totalOriginalPrice())
                    .totalDiscountAmount(result.totalDiscountAmount())
                    .totalFinalPrice(result.totalFinalPrice())
                    .build();
        }
    }

    @Builder
    public record CartItems(
            List<Detail> items
    ) {
        public static CartItems from(CartResult.CartAddResult result) {
            return CartItems.builder()
                    .items(Detail.from(result.items()))
                    .build();
        }
    }

    @Builder
    public record Detail (
            Long id,
            CartItemStatus status,
            boolean isAvailable,
            Long productId,
            Long productVariantId,
            String productName,
            String thumbnail,
            int quantity,
            CartItemPrice price,
            long lineTotal,
            List<CartItemOption> options
    ) {
        public static Detail from(CartResult.CartItemResult result) {
            return Detail.builder()
                    .id(result.id())
                    .status(result.status())
                    .isAvailable(result.isAvailable())
                    .productId(result.productId())
                    .productVariantId(result.productVariantId())
                    .productName(result.productName())
                    .thumbnail(result.thumbnail())
                    .quantity(result.quantity())
                    .price(CartItemPrice.from(result.price()))
                    .lineTotal(result.lineTotal())
                    .options(mapToOptions(result.options()))
                    .build();
        }

        public static List<Detail> from(List<CartResult.CartItemResult> results) {
            return results.stream().map(Detail::from).toList();
        }

        private static List<CartItemOption> mapToOptions(List<CartResult.CartItemOption> options) {
            return options.stream().map(CartItemOption::from).toList();
        }
    }

    @Builder
    public record CartItemPrice (
            long originalPrice,
            long discountRate,
            long discountAmount,
            long discountedPrice
    ) {
        public static CartItemPrice from(CartResult.CartItemPrice price){
            return CartItemPrice.builder()
                    .originalPrice(price.originalPrice())
                    .discountRate(price.discountRate())
                    .discountAmount(price.discountAmount())
                    .discountedPrice(price.discountedPrice())
                    .build();
        }
    }

    @Builder
    public record CartItemOption (
            String optionTypeName,
            String optionValueName
    ) {
        public static CartItemOption from (CartResult.CartItemOption option) {
            return CartItemOption.builder()
                    .optionTypeName(option.optionTypeName())
                    .optionValueName(option.optionValueName())
                    .build();
        }
    }
}
