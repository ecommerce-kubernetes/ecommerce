package com.example.order_service.infrastructure.dto.request;

import com.example.order_service.infrastructure.dto.command.CouponCommand;
import lombok.Builder;

import java.util.List;

public class CouponClientRequest {

    @Builder
    public record Calculate (
            Long userId,
            Long cartCouponId,
            List<Item> items
    ) {
        public static Calculate from(CouponCommand.Calculate command) {
            return Calculate.builder()
                    .userId(command.userId())
                    .cartCouponId(command.cartCouponId())
                    .items(Item.from(command.items()))
                    .build();
        }
    }

    @Builder
    public record Item(
            Long productVariantId,
            Long price,
            Integer quantity,
            Long itemCouponId
    ) {
        public static Item from(CouponCommand.Item command) {
            return Item.builder()
                    .productVariantId(command.productVariantId())
                    .price(command.price())
                    .quantity(command.quantity())
                    .itemCouponId(command.itemCouponId())
                    .build();
        }

        public static List<Item> from(List<CouponCommand.Item> commands) {
            return commands.stream().map(Item::from).toList();
        }
    }
}
