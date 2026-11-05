package com.example.order_service.order.adapter.out.persistence.entity;

import lombok.*;

import java.util.List;

/**
 * Redis 저장 전용 엔티티 객체

 * [WARNING]
 * 애플리케이션 및 도메인 계층에서 이 클래스를 직접 생성하거나 비즈니스 로직에서 사용을 금지.
 * Redis와의 데이터 변환은 반드시 OrderSheetRedisMapper를 통해서만 이루어져야 함.
 */
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheetItemRedisEntity {
    private Long id;
    private ProductSnapshotRedisEntity productSnapshot;
    private ProductPriceSnapshotRedisEntity priceSnapshot;
    private ItemCouponSnapshotRedisEntity itemCouponSnapshot;
    private int quantity;
    private List<ProductOptionSnapshotRedisEntity> optionSnapshots;
}
