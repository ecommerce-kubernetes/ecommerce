package com.example.order_service.order.infrastructure.messaging.dto;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.saga.domain.SagaStep;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CouponMessage {
    private SagaType type;
    private String orderNo;
    private SagaStep step;
    private CouponInfo coupon;

    @Builder
    public record CouponInfo(
            Long cartCouponId,
            List<Long> itemCouponIds
    ) {
        public static CouponInfo from(SagaPayload.CouponPayload payload) {
            return CouponInfo.builder()
                    .cartCouponId(payload.getCartCouponId())
                    .itemCouponIds(payload.getItemCouponIds())
                    .build();
        }
    }

    public static CouponMessage used(SagaMessage message) {
        return CouponMessage.builder()
                .type(SagaType.USED_COUPON)
                .orderNo(message.getOrderNo())
                .step(message.getStep())
                .coupon(CouponInfo.from(message.getPayload().getCoupon()))
                .build();
    }

    public static CouponMessage restore(SagaMessage message) {
        return CouponMessage.builder()
                .type(SagaType.RESTORE_COUPON)
                .orderNo(message.getOrderNo())
                .step(message.getStep())
                .coupon(CouponInfo.from(message.getPayload().getCoupon()))
                .build();
    }
}
