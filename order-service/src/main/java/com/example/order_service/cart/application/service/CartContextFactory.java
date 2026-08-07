package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CartContextFactory {

    public AddCartItemsContext toAddCartItemsContext(AddCartItemsCommand command, CartProductResult products) {
        Map<Long, CartProductResult.CartProductDetail> productsMap = products.toMap();
        List<AddCartItemsContext.Item> items = command.items().stream()
                .map(item -> toAddItemContext(item, productsMap.get(item.productVariantId()))).toList();
        return AddCartItemsContext.builder()
                .items(items)
                .build();
    }

    private AddCartItemsContext.Item toAddItemContext(AddCartItemsCommand.Item item, CartProductResult.CartProductDetail product) {
        return AddCartItemsContext.Item.builder()
                .productVariantId(item.productVariantId())
                .quantity(item.quantity())
                .maxLimit(product.stock())
                .build();
    }

    public UpdateCartItemContext toUpdateContext(UpdateCartItemQuantityCommand command, CartItemData cartItem,
                                                 CartProductResult.CartProductDetail product) {
        return UpdateCartItemContext.builder()
                .cartItemId(cartItem.cartItemId())
                .quantity(command.quantity())
                .maxLimit(product.stock())
                .build();
    }
}
