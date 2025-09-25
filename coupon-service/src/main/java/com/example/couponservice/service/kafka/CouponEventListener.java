package com.example.couponservice.service.kafka;

import com.example.common.*;
import com.example.couponservice.dto.CouponDto;
import com.example.couponservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CouponService couponService;

    //주문 받았을때
    @KafkaListener(topics = "order.created")
    public void processCoupon(@Payload OrderCreatedEvent event,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        if (event.getUserCouponId() == null) {
            return;
        }

        try {
            //성공
            CouponDto couponDto = couponService.useCouponByUser(event.getUserCouponId());
            kafkaTemplate.send("coupon.used.applied", key, new CouponUsedSuccessEvent(
                            event.getOrderId(),
                            event.getUserCouponId(),
                            couponDto.getDiscountType(),
                            couponDto.getDiscountValue(),
                            couponDto.getMinPurchaseAmount(),
                            couponDto.getMaxDiscountAmount()
                    )
            );
            log.info("send a failed message to coupon.used.applied");
        } catch (Exception e) {
            //실패
            log.error("쿠폰 사용 실패: orderId={}, userCouponId={}, reason={}", event.getOrderId(), event.getUserCouponId() ,e.getMessage());
            kafkaTemplate.send("coupon.used.failed", key, new FailedEvent(event.getOrderId(), e.getMessage()));
            log.info("send a failed message to coupon.used.failed");
        }
    }

    //복구
    @KafkaListener(topics = "coupon.used.cancel")
    public void revertCoupon(@Payload CouponUsedSuccessEvent event,
                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        try {
            couponService.revertUserCoupon(event.getUserCouponId());
            log.info("쿠폰 복구 : userCouponId={}", event.getUserCouponId());
        } catch (Exception ex) {
            log.error("쿠폰 복구 실패: userCouponId={}, 이유={}", event.getUserCouponId(), ex.getMessage());
        }
    }
}
