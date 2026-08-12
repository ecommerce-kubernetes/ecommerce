package com.example.order_service.saga.domain;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderSagaPayload(
        Long userId,
        List<OrderLine> orderLines,
        UsedCoupons usedCoupons,
        Money usedPoints
) {

    public record OrderLine(
            Long productVariantId,
            Integer quantity
    ) {}

    public record UsedCoupons(
            Long cartCouponId,
            List<Long> itemCouponIds
    ) {}

}
