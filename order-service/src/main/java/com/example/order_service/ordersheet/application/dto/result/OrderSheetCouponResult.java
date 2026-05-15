package com.example.order_service.ordersheet.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderSheetCouponResult {

    @Builder
    public record Calculate(
            CartCoupon cartCoupon,
            List<ItemCoupon> itemCoupons
    ) {
        public static Calculate empty() {
            return Calculate.builder()
                    .cartCoupon(null)
                    .itemCoupons(List.of())
                    .build();
        }

        public Map<Long, ItemCoupon> toItemCouponMap() {
            return itemCoupons.stream().collect(Collectors.toMap(ItemCoupon::productVariantId, Function.identity()));
        }
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
