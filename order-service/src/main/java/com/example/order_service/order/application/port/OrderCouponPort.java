package com.example.order_service.order.application.port;

import com.example.order_service.order.application.port.dto.result.CartCouponResult;
import com.example.order_service.order.application.port.dto.result.ItemCouponResult;

public interface OrderCouponPort {
    ItemCouponResult getItemCoupon(Long userId, Long itemCouponId);

    CartCouponResult getCartCoupon(Long userId, Long cartCouponId);
}
