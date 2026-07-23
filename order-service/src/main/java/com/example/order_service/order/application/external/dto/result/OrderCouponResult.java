package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.order.domain.vo.CartCouponSnapshot;
import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Deprecated
public class OrderCouponResult {

    @Builder
    public record Calculate(
            CartCouponSnapshot cartCoupon,
            List<ItemCoupon> itemCoupons
    ) {
        public static Calculate empty(){
            return Calculate.builder()
                    .cartCoupon(null)
                    .itemCoupons(List.of())
                    .build();
        }

        public Map<Long, ItemCouponSnapshot> toItemCouponMap() {
            return itemCoupons.stream().collect(Collectors.toMap(ItemCoupon::productVariantId, ItemCoupon::itemCoupon));
        }
    }

    @Builder
    public record ItemCoupon(
            Long productVariantId,
            ItemCouponSnapshot itemCoupon
    ) {}
}
