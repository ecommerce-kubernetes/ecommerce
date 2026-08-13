package com.example.order_service.saga.domain;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;
import org.springframework.util.Assert;

import java.util.List;

@Builder
public record OrderSagaPayload(
        Long userId,
        List<OrderLine> orderLines,
        UsedCoupons usedCoupons,
        Money usedPoints
) {

    @Builder
    public record OrderLine(
            Long productVariantId,
            Integer quantity
    ) {
    }

    @Builder
    public record UsedCoupons(
            Long cartCouponId,
            List<Long> itemCouponIds
    ) {
    }

    public OrderSagaPayload {
        Assert.notEmpty(orderLines, "페이로드의 주문 상품은 필수이다.");
    }

    public boolean hasCoupons() {
        boolean hasCartCoupon = this.usedCoupons.cartCouponId != null;
        boolean hasItemCoupons = this.usedCoupons.itemCouponIds != null && !usedCoupons.itemCouponIds.isEmpty();
        return hasCartCoupon || hasItemCoupons;
    }
}
