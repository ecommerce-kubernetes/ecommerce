package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderCouponResult {

    @Builder
    public record Calculate(
            OrderCouponSnapshot cartCoupon,
            List<ItemCoupon> itemCoupons
    ) {
        public static Calculate empty(){
            return Calculate.builder()
                    .cartCoupon(null)
                    .itemCoupons(List.of())
                    .build();
        }

        public Map<Long, OrderCouponSnapshot> toItemCouponMap() {
            return itemCoupons.stream().collect(Collectors.toMap(ItemCoupon::productVariantId, ItemCoupon::itemCoupon));
        }
    }

    @Builder
    public record ItemCoupon(
            Long productVariantId,
            OrderCouponSnapshot itemCoupon
    ) {}
}
