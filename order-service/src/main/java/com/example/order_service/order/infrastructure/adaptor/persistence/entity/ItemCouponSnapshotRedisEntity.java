package com.example.order_service.order.infrastructure.adaptor.persistence.entity;

public class ItemCouponSnapshotRedisEntity {
    private Long itemCouponId;
    private String name;
    private String policyType;
    private Long fixedDiscountAmount;
    private Integer discountRate;
    private Long maxDiscountAmount;
    private int applyQuantityLimit;
}
