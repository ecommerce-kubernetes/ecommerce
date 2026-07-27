package com.example.order_service.order.infrastructure.adaptor.persistence.entity;

import java.util.List;

public class OrderSheetItemRedisEntity {
    private String id;
    private ProductSnapshotRedisEntity productSnapshot;
    private ProductPriceSnapshotRedisEntity priceSnapshot;
    private ItemCouponSnapshotRedisEntity itemCouponSnapshot;
    private int quantity;
    private List<ProductOptionSnapshotRedisEntity> optionSnapshots;
}
