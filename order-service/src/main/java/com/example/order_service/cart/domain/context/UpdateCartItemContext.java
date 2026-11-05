package com.example.order_service.cart.domain.context;

import lombok.Builder;
import org.springframework.util.Assert;

@Builder
public record UpdateCartItemContext(
        Long cartItemId,
        Integer quantity,
        Integer maxLimit
) {

    public UpdateCartItemContext {
        Assert.notNull(cartItemId, "장바구니 항목 수량 변경시 장바구니 항목 식별자는 필수이다.");
        Assert.notNull(quantity, "장바구니 항목 수량 변경시 변경 수량은 필수이다.");
        Assert.notNull(maxLimit, "장바구니 항목 최대 수량은 필수이다.");
    }
}
