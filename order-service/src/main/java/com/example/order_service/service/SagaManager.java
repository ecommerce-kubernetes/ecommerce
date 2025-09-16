package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.exception.OrderVerificationException;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import com.example.order_service.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaManager {
    private final static String ORDER_CREATED_TOPIC = "order.created";
    private final KafkaProducer kafkaProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderService orderService;

    // 트랜잭션이 커밋된 이후에 메서드를 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePendingOrderCreated(PendingOrderCreatedEvent event){
        OrderCreatedEvent orderEvent = createOrderEvent(event);
        kafkaProducer.sendMessage(ORDER_CREATED_TOPIC, orderEvent);
        storeOrderDataToRedis(event);
    }

    public void processSaga(String sagaKey){
        Map<Object, Object> sagaState = redisTemplate.opsForHash().entries(sagaKey);
        Set<String> requiredField = Set.of("product", "user", "coupon");
        if(sagaState.keySet().containsAll(requiredField)){
            try {
                orderService.finalizeOrder(sagaState);
            } catch (OrderVerificationException e){
                //TODO 롤백
            }
        }
    }

    // redis Hash로 주문 생성 데이터를 저장 및 타임 아웃 시간 설정
    private void storeOrderDataToRedis(PendingOrderCreatedEvent event){
        String sagaKey = "saga:order:" + event.getOrderId();

        Map<String, Object> initialSagaState = new HashMap<>();
        initialSagaState.put("orderId", event.getOrderId());
        initialSagaState.put("status", event.getStatus());
        initialSagaState.put("createdAt", event.getCreatedAt().toString());
        redisTemplate.opsForHash().putAll(sagaKey, initialSagaState);

        addOrderToTimeout(event.getOrderId(), 5, TimeUnit.MINUTES);
    }

    private void addOrderToTimeout(Long orderId, long timeout, TimeUnit timeUnit){
        String key = "saga:timeouts";

        long timeMilliSeconds = timeUnit.toMillis(timeout);
        double score = System.currentTimeMillis() + timeMilliSeconds;

        redisTemplate.opsForZSet().add(key, orderId, score);
    }

    private OrderCreatedEvent createOrderEvent(PendingOrderCreatedEvent event){
        OrderRequest request = event.getOrderRequest();
        int useReserve = request.getUseToReserve() != null ? request.getUseToReserve() : 0;

        return new OrderCreatedEvent(
                event.getOrderId(),
                event.getUserId(),
                request.getCouponId(),
                event.getOrderProducts(),
                (request.getUseToReserve() != null && request.getUseToReserve() !=0),
                useReserve,
                request.getUseToCash(),
                request.getUseToCash() + useReserve
        );
    }
}
