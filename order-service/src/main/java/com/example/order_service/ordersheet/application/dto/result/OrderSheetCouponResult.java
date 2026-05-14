package com.example.order_service.ordersheet.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.ordersheet.domain.model.vo.CouponStatus;
import lombok.Builder;

import java.util.List;

public class OrderSheetCouponResult {

    @Builder
    public record Calculate(
            CartCoupon cartCoupon,
            List<ItemCoupon> itemCoupons
    ) {
    }

    @Builder
    public record CartCoupon(
            Long couponId,
            String couponName,
            Money discountAmount
    ) {
    }

    @Builder
    public record ItemCoupon(
            Long productVariantId,
            Long couponId,
            String couponName,
            Money discountAmount
    ) {
    }

}
