package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;

import java.util.List;

public interface OrderCouponPort {
    ItemCouponResult getItemCoupon(Long userId, Long itemCouponId);

    ItemCouponsResult getItemCoupons(Long userId, List<Long> itemCouponIds);

    CartCouponResult getCartCoupon(Long userId, Long cartCouponId);
}
