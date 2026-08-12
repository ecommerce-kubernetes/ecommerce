package com.example.order_service.saga.domain.tmp;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.ItemPayloadListConverter;
import com.example.order_service.common.util.LongListConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Deprecated
@Getter
@Embeddable
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaPayloadDeprecated {
    private Long userId;
    @Convert(converter = ItemPayloadListConverter.class)
    private List<ItemPayload> items;
    @Embedded
    private CouponPayload coupon;
    @Embedded
    private PointPayload points;

    public static SagaPayloadDeprecated of(Long userId, List<ItemPayload> items, CouponPayload coupon, PointPayload points) {
        return new SagaPayloadDeprecated(userId, items, coupon, points);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ItemPayload {
        private Long productVariantId;
        private Integer quantity;

        public static ItemPayload of(Long productVariantId, Integer quantity) {
            return new ItemPayload(productVariantId, quantity);
        }
    }

    @Getter
    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CouponPayload {
        private Long cartCouponId;
        @Convert(converter = LongListConverter.class)
        private List<Long> itemCouponIds;

        public static CouponPayload of(Long cartCouponId, List<Long> itemCouponIds) {
            return new CouponPayload(cartCouponId, itemCouponIds);
        }
    }

    @Getter
    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PointPayload {
        private Money usedPoints;

        public static PointPayload of(Money usedPoints) {
            return new PointPayload(usedPoints);
        }
    }
}
