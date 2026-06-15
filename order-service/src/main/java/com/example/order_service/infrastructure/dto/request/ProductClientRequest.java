package com.example.order_service.infrastructure.dto.request;

import com.example.order_service.infrastructure.dto.command.ProductCommand;
import lombok.Builder;

import java.util.List;

public class ProductClientRequest {

    @Builder
    public record BulkSearch(
            List<Long> variantIds
    ) {

        public static BulkSearch from(List<Long> variantIds) {
            return BulkSearch.builder()
                    .variantIds(variantIds)
                    .build();
        }
    }

    @Builder
    public record Validate(
            List<Item> items
    ) {
        public static Validate from(ProductCommand.Validate command) {
            return Validate.builder()
                    .items(Item.from(command.items()))
                    .build();
        }
    }

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity
    ) {
        public static Item from(ProductCommand.Item command) {
            return Item.builder()
                    .productVariantId(command.productVariantId())
                    .quantity(command.quantity())
                    .build();
        }

        public static List<Item> from(List<ProductCommand.Item> commands) {
            return commands.stream().map(Item::from).toList();
        }
    }
}
