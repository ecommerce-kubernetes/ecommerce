package com.example.order_service.cart.domain.context;

import com.mysema.commons.lang.Assert;
import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsContext(
        List<Item> items
) {

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity,
            Integer maxLimit
    ) {

        public Item {
            Assert.notNull(productVariantId, "상품 판매 단위 식별자는 필수이다.");
            Assert.notNull(quantity, "장바구니 항목 수량은 필수이다.");
            Assert.notNull(maxLimit, "장바구니 항목 최대 추가 수량은 필수이다.");
        }
    }

    public AddCartItemsContext {
        Assert.notNull(items, "장바구니에 추가할 항목은 필수이다.");
    }
}
