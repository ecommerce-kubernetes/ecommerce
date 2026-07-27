package com.example.order_service.order.infrastructure.adaptor.persistence.entity;

import java.util.List;

public class OrderSheetRedisEntity {
    private String id;
    private OrdererRedisEntity orderer;
    private ShippingAddressRedisEntity shippingAddress;
    private List<OrderSheetItemRedisEntity> items;
    private CartCouponRedisEntity cartCoupon;
    private Long usedPoints;
    private String expiresAt;

}
