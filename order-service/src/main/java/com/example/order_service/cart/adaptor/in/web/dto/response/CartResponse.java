package com.example.order_service.cart.adaptor.in.web.dto.response;

import com.example.order_service.cart.application.service.dto.result.CartItemOption;
import com.example.order_service.cart.application.service.dto.result.CartItemPrice;
import com.example.order_service.cart.application.service.dto.result.CartItemResult;
import com.example.order_service.cart.application.service.dto.result.CartResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record CartResponse(
        List<Item> items,
        long totalCount
) {

    @Builder
    public record Item(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
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
        public static Item from(CartItemResult item) {
            return Item.builder()
                    .cartItemId(item.cartItemId())
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

        public static List<Item> from(List<CartItemResult> items) {
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

        public static Price from(CartItemPrice price) {
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
        public static Option from(CartItemOption option) {
            return Option.builder()
                    .optionTypeName(option.optionTypeName())
                    .optionValueName(option.optionValueName())
                    .build();
        }

        public static List<Option> from(List<CartItemOption> options) {
            return options.stream().map(Option::from).toList();
        }
    }

    public static CartResponse from(CartResult result) {
        return CartResponse.builder()
                .items(Item.from(result.items()))
                .totalCount(result.items().size())
                .build();
    }
}
