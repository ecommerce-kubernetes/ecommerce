package com.example.payment_service.service.kafka;

import com.example.common.*;
import com.example.payment_service.event.EventStatus;
import com.example.payment_service.event.EventStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventStore eventStore;
    private final ObjectMapper objectMapper;

    private static final String USER_EVENT_PREFIX = "user.cache.deducted.";
    private static final String COUPON_EVENT_PREFIX = "coupon.used.applied.";
    private static final String PRODUCT_EVENT_PREFIX = "product.stock.deducted.";
    private static final String PAYMENT_EVENT_PREFIX = "payment.event.";

    @KafkaListener(topics = "user.cache.deducted")
    public void handleUserCacheDeductedEvent(@Payload UserCacheDeductedEvent event,
                                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        String eventKey = getEventKey(topic, key);
        eventStore.createEvent(eventKey, event);
        eventStore.markEventAsSuccess(eventKey);

        attemptPayment(key);
    }

    @KafkaListener(topics = "coupon.used.applied")
    public void handleCouponUsedSuccessEvent(@Payload CouponUsedSuccessEvent event,
                                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        String eventKey = getEventKey(topic, key);
        eventStore.createEvent(eventKey, event);
        eventStore.markEventAsSuccess(eventKey);

        attemptPayment(key);
    }

    @KafkaListener(topics = "product.stock.deducted")
    public void handleProductStockDeductedEvent(@Payload ProductStockDeductedEvent event,
                                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        String eventKey = getEventKey(topic, key);
        eventStore.createEvent(eventKey, event);
        eventStore.markEventAsSuccess(eventKey);

        attemptPayment(key);
    }

    // 결제 성공 여부 판단 및 메시지 전송
    private void attemptPayment(String key) {

        //실패이벤트가 있는지 확인 후 있으면 롤백 아니면 결제로직 실행
        if (eventStore.getEventStatus(USER_EVENT_PREFIX + key) == EventStatus.FAILED ||
                eventStore.getEventStatus(COUPON_EVENT_PREFIX + key) == EventStatus.FAILED ||
                eventStore.getEventStatus(PRODUCT_EVENT_PREFIX + key) == EventStatus.FAILED
        ) {
            rollbackIfNeeded(key);
        } else {
            //모든 이벤트가 성공적인지 확인
            if (isAllEventsSuccessful(key)) {
                log.info("Not all success messages have arrived yet for key {}", key);
                return;
            }

            //이미 수행했는지 확인
            if (!eventStore.tryMarkProcessing(PAYMENT_EVENT_PREFIX + key)) {
                log.info("Payment already being processed for key {}", key);
                return;
            }

            UserCacheDeductedEvent userEvent = objectMapper.convertValue(eventStore.getEventObject(USER_EVENT_PREFIX + key), UserCacheDeductedEvent.class);
            CouponUsedSuccessEvent couponEvent = objectMapper.convertValue(eventStore.getEventObject(COUPON_EVENT_PREFIX + key), CouponUsedSuccessEvent.class);
            ProductStockDeductedEvent productEvent = objectMapper.convertValue(eventStore.getEventObject(PRODUCT_EVENT_PREFIX + key), ProductStockDeductedEvent.class);

            int calculatedAmount = calculateTotalAmount(productEvent, couponEvent);

            if (calculatedAmount == userEvent.getExpectTotalAmount()) {
                log.info("send a success message to payment.success");
                kafkaTemplate.send("payment.success", key, new PaymentSuccessEvent(userEvent.getOrderId()));
            } else {
                log.info("send a failed message to payment.failed");
                kafkaTemplate.send("payment.failed", key, new PaymentFailedEvent(userEvent.getOrderId(), "결제 정보가 맞지 않습니다."));
            }
        }

    }

    // 결제 금액 계산
    private int calculateTotalAmount(ProductStockDeductedEvent productEvent, CouponUsedSuccessEvent couponEvent) {
        int total = productEvent.getProductList().stream()
                .mapToInt(p -> p.getDiscountPrice() * p.getCount())
                .sum();

        if (couponEvent.getDiscountType() == DiscountType.AMOUNT) {
            if (couponEvent.getMinPurchaseAmount() <= total) {
                total -= couponEvent.getDiscountValue();
            }
        } else {
            BigDecimal discountRate = BigDecimal.valueOf(couponEvent.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);

            BigDecimal discount = BigDecimal.valueOf(total).multiply(discountRate);
            BigDecimal maxDiscount = BigDecimal.valueOf(couponEvent.getMaxDiscountAmount());

            if (discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }

            total = BigDecimal.valueOf(total).subtract(discount).intValue();
        }

        return total;
    }


    @KafkaListener(topics = "user.cache.failed")
    public void handleUserCacheFailedEvent(@Payload PaymentFailedEvent event,
                                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        //실패 이벤트를 받고 redis에 상태 실패로 저장
        String eventKey = getEventKey("user.cache.deducted", key);
        eventStore.createEvent(eventKey, event);
        eventStore.markEventAsFailed(eventKey);
        //롤백 로직 실행
        rollbackIfNeeded(key);
    }

    @KafkaListener(topics = "coupon.used.failed")
    public void handleCouponUsedFailedEvent(@Payload PaymentFailedEvent event,
                                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        //실패 이벤트를 받고 redis에 상태 실패로 저장
        String eventKey = getEventKey("coupon.used.applied", key);
        eventStore.createEvent(eventKey, event);
        eventStore.markEventAsFailed(eventKey);
        //롤백 로직 실행
        rollbackIfNeeded(key);
    }

    @KafkaListener(topics = "product.stock.failed")
    public void handleProductStockFailedEvent(@Payload PaymentFailedEvent event,
                                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        //실패 이벤트를 받고 redis에 상태 실패로 저장
        String eventKey = getEventKey("product.stock.deduct", key);
        eventStore.createEvent(eventKey, event);
        eventStore.markEventAsFailed(eventKey);
        //롤백 로직 실행
        rollbackIfNeeded(key);
    }


    // 이벤트 수신 여부 확인
    private boolean isAllEventsReceived(String key) {
        EventStatus userStatus = eventStore.getEventStatus(USER_EVENT_PREFIX + key);
        EventStatus couponStatus = eventStore.getEventStatus(COUPON_EVENT_PREFIX + key);
        EventStatus productStatus = eventStore.getEventStatus(PRODUCT_EVENT_PREFIX + key);

        return (userStatus != null && userStatus != EventStatus.PENDING) &&
                (couponStatus != null && couponStatus != EventStatus.PENDING) &&
                (productStatus != null && productStatus != EventStatus.PENDING);
    }

    // 모든 성공 상태인지 확인
    private boolean isAllEventsSuccessful(String key) {
        return eventStore.getEventStatus(USER_EVENT_PREFIX + key) == EventStatus.SUCCESS &&
                eventStore.getEventStatus(COUPON_EVENT_PREFIX + key) == EventStatus.SUCCESS &&
                eventStore.getEventStatus(PRODUCT_EVENT_PREFIX + key) == EventStatus.SUCCESS;
    }

    // 이벤트 롤백 처리
    private void rollbackIfNeeded(String key) {

        //모든 메세지가 왔는지 확인
        if (!isAllEventsReceived(key)) return;

        //롤백이 이미 실행했는지 redis에서 찾기
        if (!eventStore.tryMarkRollback(PAYMENT_EVENT_PREFIX + key)) {
            log.info("Payment already being rollbacked for key {}", key);
            return;
        }

        //롤백실행
        if (eventStore.getEventStatus(USER_EVENT_PREFIX + key) == EventStatus.SUCCESS) {
            UserCacheDeductedEvent eventObject = objectMapper.convertValue(eventStore.getEventObject(USER_EVENT_PREFIX + key), UserCacheDeductedEvent.class);
            kafkaTemplate.send("user.cache.restore", key, eventObject);
            log.info("send a success message to user.cache.restore");
        }

        if (eventStore.getEventStatus(COUPON_EVENT_PREFIX + key) == EventStatus.SUCCESS) {
            CouponUsedSuccessEvent eventObject = objectMapper.convertValue(eventStore.getEventObject(COUPON_EVENT_PREFIX + key), CouponUsedSuccessEvent.class);
            kafkaTemplate.send("coupon.used.cancel", key, eventObject);
            log.info("send a success message to coupon.used.cancel");
        }

        if (eventStore.getEventStatus(PRODUCT_EVENT_PREFIX + key) == EventStatus.SUCCESS) {
            ProductStockDeductedEvent eventObject = objectMapper.convertValue(eventStore.getEventObject(PRODUCT_EVENT_PREFIX + key), ProductStockDeductedEvent.class);
            kafkaTemplate.send("product.stock.restore", key, eventObject);
            log.info("send a success message to product.stock.restore");
        }

        //kafka 메세지
        kafkaTemplate.send("payment.failed", key, new PaymentFailedEvent(Long.parseLong(key), "결제 실패."));
    }

    // 토픽에 따른 키 prefix 매핑
    private String getEventKey(String topic, String key) {
        switch (topic) {
            case "user.cache.deducted":
                return USER_EVENT_PREFIX + key;
            case "coupon.used.applied":
                return COUPON_EVENT_PREFIX + key;
            case "product.stock.deducted":
                return PRODUCT_EVENT_PREFIX + key;
            default:
                throw new IllegalArgumentException("Unknown topic: " + topic);
        }
    }
}
