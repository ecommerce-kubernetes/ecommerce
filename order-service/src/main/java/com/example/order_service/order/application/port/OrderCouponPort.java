package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponResult;

public interface OrderCouponPort {
    ItemCouponResult getItemCoupon(Long userId, Long itemCouponId);

    CartCouponResult getCartCoupon(Long userId, Long cartCouponId);
}
