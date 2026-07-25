package com.example.order_service.cart.application.dto.param;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record CreateCartItemsContext(
        Long userId,
        List<Item> items
) {

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity,
            Integer maxLimit
    ) {
    }

    public static CreateCartItemsContext of(AddCartItemsCommand command, CartProductResult products) {
        Map<Long, CartProductResult.CartProductDetail> productsMap = products.toMap();
        List<CreateCartItemsContext.Item> items = command.items().stream().map(item -> {
            CartProductResult.CartProductDetail product = productsMap.get(item.productVariantId());
            return CreateCartItemsContext.Item.builder()
                    .productVariantId(product.productVariantId())
                    .quantity(item.quantity())
                    .maxLimit(product.stock())
                    .build();
        }).toList();
        return CreateCartItemsContext.builder()
                .userId(command.userId())
                .items(items)
                .build();
    }
}
