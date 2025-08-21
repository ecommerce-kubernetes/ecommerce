package com.example.userservice.service.kafka;

import com.example.common.OrderCreatedEvent;
import com.example.common.PaymentFailedEvent;
import com.example.common.UserCacheDeductedEvent;
import com.example.userservice.service.UserService;
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
public class UserEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserService userService;

    //주문 받았을때
    @KafkaListener(topics = "order.created")
    public void processCache(@Payload OrderCreatedEvent event,
                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        if (event.isPointUsage()) {
            try {
                userService.deductPoint(event.getUserId(), event.getReservedPointAmount());
            } catch (Exception e) {
                log.error("포인트 차감 실패: userId={}, orderId={}, 이유={}", event.getUserId(), event.getOrderId(), e.getMessage());
                kafkaTemplate.send("user.cache.failed", key, new PaymentFailedEvent(event.getOrderId(), e.getMessage()));
                log.info("send a success message to user.cache.failed");
            }
        }

        try {
            userService.deductCache(event.getUserId(), event.getReservedCacheAmount());
            kafkaTemplate.send("user.cache.deducted", key, new UserCacheDeductedEvent(
                    event.getOrderId(),
                    event.getUserId(),
                    event.isPointUsage(),
                    event.getReservedPointAmount(),
                    event.getReservedCacheAmount(),
                    event.getExpectTotalAmount())
            );
            log.info("send a success message to user.cache.deducted");
        } catch (Exception e) {
            log.error("캐시 차감 실패: userId={}, orderId={}, 이유={}", event.getUserId(), event.getOrderId(), e.getMessage());
            if (event.isPointUsage()) {
                try {
                    userService.rechargePoint(event.getUserId(), event.getReservedPointAmount());
                    log.info("포인트 복구 완료: userId={}, point={}", event.getUserId(), event.getReservedPointAmount());
                } catch (Exception rollbackEx) {
                    log.error("포인트 복구 실패: userId={}, point={}, 이유={}",
                            event.getUserId(), event.getReservedPointAmount(), rollbackEx.getMessage());
                }
            }
            kafkaTemplate.send("user.cache.failed", key, new PaymentFailedEvent(event.getOrderId(), e.getMessage()));
            log.info("send a success message to user.cache.failed");
        }
    }

    //복구
    @KafkaListener(topics = "user.cache.restore")
    public void revertCache(@Payload UserCacheDeductedEvent event,
                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        if (event.isPointUsage()) {
            try {
                userService.rechargePoint(event.getUserId(), event.getReservedPointAmount());
                log.info("포인트 복구 : userId={}, point={}", event.getUserId(), event.getReservedPointAmount());
            } catch (Exception ex) {
                log.error("포인트 복구 실패: userId={}, point={}, 이유={}", event.getUserId(), event.getReservedPointAmount(), ex.getMessage());
            }
        }

        try {
            userService.rechargeCache(event.getUserId(), event.getReservedCacheAmount());
            log.info("캐시 복구 : userId={}, cache={}", event.getUserId(), event.getReservedCacheAmount());
        } catch (Exception ex) {
            log.error("캐시 복구 실패: userId={}, cache={}, 이유={}", event.getUserId(), event.getReservedCacheAmount(), ex.getMessage());
        }
    }
}
