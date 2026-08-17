package com.example.order_service.saga.adapter.out.message.processor.dto;

import com.example.order_service.saga.domain.event.RestoreCouponEvent;
import com.example.order_service.saga.domain.event.UsedCouponEvent;
import lombok.Builder;

import java.util.List;

@Builder
public record CouponMessagePayload(
        Long executionId,
        Long userId,
        Long cartCouponId,
        List<Long> itemCouponIds
) {

    public static CouponMessagePayload from(UsedCouponEvent event) {
        return CouponMessagePayload.builder()
                .executionId(event.executionId())
                .userId(event.userId())
                .cartCouponId(event.coupons().cartCouponId())
                .itemCouponIds(event.coupons().itemCouponIds())
                .build();
    }

    public static CouponMessagePayload from(RestoreCouponEvent event) {
        return CouponMessagePayload.builder()
                .executionId(event.executionId())
                .userId(event.userId())
                .cartCouponId(event.coupons().cartCouponId())
                .itemCouponIds(event.coupons().itemCouponIds())
                .build();
    }
}
