package com.example.order_service.cart.adapter.in.web.dto.response;

import com.example.order_service.cart.application.service.dto.result.CartItemOption;
import com.example.order_service.cart.application.service.dto.result.CartItemPrice;
import com.example.order_service.cart.application.service.dto.result.CartItemResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record CartItemResponse(
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

    public static CartItemResponse from(CartItemResult result) {
        return CartItemResponse.builder()
                .cartItemId(result.cartItemId())
                .status(result.status().name())
                .productId(result.productId())
                .productVariantId(result.productVariantId())
                .productName(result.productName())
                .thumbnail(result.thumbnail())
                .quantity(result.quantity())
                .price(Price.from(result.price()))
                .lineTotal(result.lineTotal().longValue())
                .options(Option.from(result.options()))
                .build();
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
}
