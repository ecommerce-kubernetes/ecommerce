package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.domain.service.dto.result.OrderCouponInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;
import com.example.order_service.api.order.infrastructure.client.coupon.OrderCouponAdaptor;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCouponService {
    private final OrderCouponAdaptor orderCouponAdaptor;

    public OrderCouponInfo calculateCouponDiscount(Long userId, Long couponId, OrderProductAmount productAmount){
        if (couponId == null) {
            return OrderCouponInfo.notUsed();
        }

        OrderCouponDiscountResponse coupon = orderCouponAdaptor.calculateDiscount(userId, couponId, productAmount.getSubTotalAmount());
        return mapToInfo(coupon);
    }

    private OrderCouponInfo mapToInfo(OrderCouponDiscountResponse coupon) {
        return OrderCouponInfo.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .discountAmount(coupon.getDiscountAmount())
                .build();
    }
}
