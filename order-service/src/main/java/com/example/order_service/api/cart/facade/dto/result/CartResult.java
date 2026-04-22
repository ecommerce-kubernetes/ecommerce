package com.example.order_service.api.cart.facade.dto.result;

import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
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
    public record CartItemResult(
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
        public static CartItemResult of(CartItemDto cartItemDto, CartProductInfo product, CartItemStatus status) {
            return CartItemResult.builder()
                    .id(cartItemDto.getId())
                    .status(status)
                    .isAvailable(status == CartItemStatus.AVAILABLE)
                    .productId(product.getProductId())
                    .productVariantId(product.getProductVariantId())
                    .productName(product.getProductName())
                    .thumbnail(product.getThumbnail())
                    .quantity(cartItemDto.getQuantity())
                    .price(CartItemPrice.from(product))
                    .lineTotal(product.getDiscountedPrice() * cartItemDto.getQuantity())
                    .options(mapToOptions(product.getProductOption()))
                    .build();
        }

        public static CartItemResult unAvailable(Long id, Long productVariantId, int quantity) {
            return CartItemResult.builder()
                    .id(id)
                    .status(CartItemStatus.NOT_FOUND)
                    .isAvailable(false)
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .lineTotal(0)
                    .build();
        }

        private static List<CartItemOption> mapToOptions(List<CartProductInfo.ProductOption> options) {
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
        public static CartItemPrice from(CartProductInfo product) {
            return CartItemPrice.builder()
                    .originalPrice(product.getOriginalPrice())
                    .discountRate(product.getDiscountRate())
                    .discountAmount(product.getDiscountAmount())
                    .discountedPrice(product.getDiscountedPrice())
                    .build();
        }
    }

    @Builder
    public record CartItemOption (
            String optionTypeName,
            String optionValueName
    ) {
        public static CartItemOption from (CartProductInfo.ProductOption option) {
            return CartItemOption.builder()
                    .optionTypeName(option.getOptionTypeName())
                    .optionValueName(option.getOptionValueName())
                    .build();
        }
    }
}
