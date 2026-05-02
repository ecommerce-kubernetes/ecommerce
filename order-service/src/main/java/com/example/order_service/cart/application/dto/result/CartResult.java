package com.example.order_service.cart.application.dto.result;

import com.example.order_service.cart.domain.service.dto.result.CartItemDto;
import lombok.Builder;

import java.util.List;

public class CartResult {

    @Builder
    public record Cart (
            List<CartItemResult> items
    ) {
        public static Cart empty() {
            return Cart.builder()
                    .items(List.of())
                    .build();
        }

        public static Cart from(List<CartItemResult> items) {
            if(items == null || items.isEmpty()){
                return empty();
            }

            return Cart.builder()
                    .items(items)
                    .build();
        }
    }

    @Builder
    public record Update (
            Long id,
            int quantity
    ) {
        public static Update from(CartItemDto cartItemDto) {
            return Update.builder()
                    .id(cartItemDto.getId())
                    .quantity(cartItemDto.getQuantity())
                    .build();
        }
    }

    @Builder
    public record CartItemResult(
            Long id,
            ProductStatus status,
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
        public static CartItemResult of(CartItemDto cartItemDto, CartProductResult.Info product) {
            return CartItemResult.builder()
                    .id(cartItemDto.getId())
                    .status(product.status())
                    .isAvailable(product.status() == ProductStatus.AVAILABLE)
                    .productId(product.productId())
                    .productVariantId(product.productVariantId())
                    .productName(product.productName())
                    .thumbnail(product.thumbnail())
                    .quantity(cartItemDto.getQuantity())
                    .price(CartItemPrice.from(product))
                    .lineTotal(product.discountedPrice() * cartItemDto.getQuantity())
                    .options(mapToOptions(product.options()))
                    .build();
        }

        public static CartItemResult unAvailable(Long id, Long productVariantId, int quantity) {
            return CartItemResult.builder()
                    .id(id)
                    .status(ProductStatus.UNAVAILABLE)
                    .isAvailable(false)
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .lineTotal(0)
                    .build();
        }

        private static List<CartItemOption> mapToOptions(List<CartProductResult.Option> options) {
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
        public static CartItemPrice from(CartProductResult.Info product) {
            return CartItemPrice.builder()
                    .originalPrice(product.originalPrice())
                    .discountRate(product.discountRate())
                    .discountAmount(product.discountAmount())
                    .discountedPrice(product.discountedPrice())
                    .build();
        }
    }

    @Builder
    public record CartItemOption (
            String optionTypeName,
            String optionValueName
    ) {
        public static CartItemOption from (CartProductResult.Option option) {
            return CartItemOption.builder()
                    .optionTypeName(option.optionTypeName())
                    .optionValueName(option.optionValueName())
                    .build();
        }
    }
}
