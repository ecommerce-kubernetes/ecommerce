package com.example.order_service.cart.domain.fixture;

import com.example.order_service.cart.domain.Cart;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.common.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class CartFixtureBuilder {

    private Long userId = 1L;
    private List<AddCartItemsContext.Item> items = new ArrayList<>();
    private static AtomicLong idSeq = new AtomicLong(100L);
    private IdGenerator idGenerator = idSeq::getAndIncrement;

    public static CartFixtureBuilder given() {
        return new CartFixtureBuilder();
    }

    public CartFixtureBuilder withCartItem(Long productVariantId, int quantity) {
        AddCartItemsContext.Item item = AddCartItemsContext.Item.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .maxLimit(quantity + 100)
                .build();
        items.add(item);

        return this;
    }

    public CartFixtureBuilder withMaxCartItem() {
        for(long productVariantId = 1; productVariantId<=20; productVariantId++) {
            AddCartItemsContext.Item item = AddCartItemsContext.Item.builder()
                    .productVariantId(productVariantId)
                    .quantity(10)
                    .maxLimit(100)
                    .build();
            items.add(item);
        }

        return this;
    }

    public Cart build() {
        Cart cart = Cart.create(userId, idGenerator);
        if (!items.isEmpty()){
            AddCartItemsContext itemsContext = AddCartItemsContext.builder()
                    .items(items)
                    .build();
            cart.addItems(itemsContext, idGenerator);
        }
        return cart;
    }
}
