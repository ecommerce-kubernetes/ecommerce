package com.example.order_service.order.infrastructure.persistence;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
class OrderSheetRedisEntity {
    private String sheetId;
    private OrderSheetOrdererRedisEntity orderer;
    private OrderSheetShippingAddressRedisEntity shippingAddress;
    private List<OrderSheetItemRedisEntity> items;
    private CouponSnapshotRedisEntity cartCoupon;
    private long totalOriginalPrice;
    private long totalProductDiscountAmount;
    private long totalCouponDiscountAmount;
    private long usedPoints;
    private long totalPaymentAmount;
    private LocalDateTime expiresAt;

    @Getter
    @Builder
    static class OrderSheetOrdererRedisEntity {
        private Long userId;
        private String userName;
        private String phoneNumber;
    }

    @Getter
    @Builder
    static class OrderSheetShippingAddressRedisEntity {
        private String receiverName;
        private String receiverPhone;
        private String zipCode;
        private String address;
        private String addressDetail;
    }

    @Getter
    @Builder
    static class OrderSheetItemRedisEntity {
        private String sheetItemId;
        private ProductSnapshotRedisEntity productSnapshot;
        private PriceSnapshotRedisEntity priceSnapshot;
        private CouponSnapshotRedisEntity itemCoupon;
        private int quantity;
        private List<OptionSnapshot> options;
    }

    @Getter
    @Builder
    static class ProductSnapshotRedisEntity {
        private Long productId;
        private Long productVariantId;
        private String sku;
        private String productName;
        private String thumbnail;
    }

    @Getter
    @Builder
    static class PriceSnapshotRedisEntity {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;
    }

    @Getter
    @Builder
    static class CouponSnapshotRedisEntity {
        private Long couponId;
        private String couponName;
        private Long discountAmount;
    }

    @Getter
    @Builder
    static class OptionSnapshot {
        private String optionTypeName;
        private String optionValueName;
    }
}
