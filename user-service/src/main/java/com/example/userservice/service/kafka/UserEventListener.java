package com.example.userservice.service.kafka;

import com.example.common.FailedEvent;
import com.example.common.OrderCreatedEvent;
import com.example.common.UserCashDeductedEvent;
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
    public void processCash(@Payload OrderCreatedEvent event,
                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Successfully received the message for {}", topic);

        if (event.isPointUsage()) {
            try {
                userService.deductPoint(event.getUserId(), event.getReservedPointAmount());
            } catch (Exception e) {
                log.error("포인트 차감 실패: userId={}, orderId={}, 이유={}", event.getUserId(), event.getOrderId(), e.getMessage());
                kafkaTemplate.send("user.cash.failed", key, new FailedEvent(event.getOrderId(), e.getMessage()));
                log.info("send a success message to user.cash.failed");
            }
        }

        try {
            userService.deductCash(event.getUserId(), event.getReservedCashAmount());
            kafkaTemplate.send("user.cash.deducted", key, new UserCashDeductedEvent(
                    event.getOrderId(),
                    event.getUserId(),
                    event.isPointUsage(),
                    event.getReservedPointAmount(),
                    event.getReservedCashAmount(),
                    event.getExpectTotalAmount())
            );
            log.info("send a success message to user.cash.deducted");
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
            kafkaTemplate.send("user.cash.failed", key, new FailedEvent(event.getOrderId(), e.getMessage()));
            log.info("send a success message to user.cash.failed");
        }
    }

    //복구
    @KafkaListener(topics = "user.cash.restore")
    public void revertCash(@Payload UserCashDeductedEvent event,
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
            userService.rechargeCash(event.getUserId(), event.getReservedCashAmount());
            log.info("캐시 복구 : userId={}, cash={}", event.getUserId(), event.getReservedCashAmount());
        } catch (Exception ex) {
            log.error("캐시 복구 실패: userId={}, cash={}, 이유={}", event.getUserId(), event.getReservedCashAmount(), ex.getMessage());
        }
    }
}
