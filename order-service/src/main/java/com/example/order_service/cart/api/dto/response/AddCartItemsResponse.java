package com.example.order_service.cart.api.dto.response;

import com.example.order_service.cart.application.service.dto.result.CartResult;
import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsResponse(
        List<Item> items
) {

    @Builder
    public record Item(
            Long cartItemId,
            String status,
            Long productId,
            Long productVariantId,
            String productName,
            String thumbnail,
            int quantity,
            Price price,
            long lineTotal,
            List<Option> options
    ) {
        public static Item from(CartResult.CartItemResult item) {
            return Item.builder()
                    .cartItemId(item.id())
                    .status(item.status().name())
                    .productId(item.productId())
                    .productVariantId(item.productVariantId())
                    .productName(item.productName())
                    .thumbnail(item.thumbnail())
                    .quantity(item.quantity())
                    .price(Price.from(item.price()))
                    .lineTotal(item.lineTotal().longValue())
                    .options(Option.from(item.options()))
                    .build();
        }

        public static List<Item> from(List<CartResult.CartItemResult> items) {
            return items.stream().map(Item::from).toList();
        }
    }

    @Builder
    public record Price(
            long originalPrice,
            long discountRate,
            long discountAmount,
            long discountedPrice
    ) {

        public static Price from(CartResult.CartItemPrice price) {
            return Price.builder()
                    .originalPrice(price.originalPrice().longValue())
                    .discountRate(price.discountRate())
                    .discountAmount(price.discountAmount().longValue())
                    .discountedPrice(price.discountedPrice().longValue())
                    .build();
        }
    }

    @Builder
    public record Option(
            String optionTypeName,
            String optionValueName
    ) {
        public static Option from(CartResult.CartItemOption option) {
            return Option.builder()
                    .optionTypeName(option.optionTypeName())
                    .optionValueName(option.optionValueName())
                    .build();
        }

        public static List<Option> from(List<CartResult.CartItemOption> options) {
            return options.stream().map(Option::from).toList();
        }
    }

    public static AddCartItemsResponse from(CartResult.Cart result) {
        return AddCartItemsResponse.builder()
                .items(Item.from(result.items()))
                .build();
    }
}
