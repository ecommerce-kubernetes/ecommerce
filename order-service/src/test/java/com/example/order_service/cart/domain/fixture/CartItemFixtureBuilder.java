package com.example.order_service.cart.domain.fixture;

import com.example.order_service.cart.domain.CartItem;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.common.util.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;

public class CartItemFixtureBuilder {

    private Long productVariantId = 1L;
    private int quantity = 1;
    private int maxLimit = 100;

    private AtomicLong idSeq = new AtomicLong(100L);
    private IdGenerator idGenerator = idSeq::getAndIncrement;

    public static CartItemFixtureBuilder given(){
        return new CartItemFixtureBuilder();
    }

    public CartItemFixtureBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public CartItemFixtureBuilder withProductVariantId(Long productVariantId) {
        this.productVariantId = productVariantId;
        return this;
    }

    public CartItemFixtureBuilder withMaxLimit(int maxLimit) {
        this.maxLimit = maxLimit;
        return this;
    }

    public CartItem build() {
        AddCartItemsContext.Item ctx = AddCartItemsContext.Item.builder()
                .productVariantId(this.productVariantId)
                .quantity(this.quantity)
                .maxLimit(this.maxLimit)
                .build();
        return CartItem.create(ctx, idGenerator);
    }
}
