package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.exception.OrderVerificationException;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import com.example.order_service.service.kafka.KafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final static String USER_ROLLBACK_TOPIC = "user.cache.restore";
    private final static String COUPON_ROLLBACK_TOPIC = "coupon.used.cancel";
    private final static String PRODUCT_ROLLBACK_TOPIC = "product.stock.restore";
    private final static String ZSET_PREFIX = "saga:timeouts";
    private final static String HASH_PREFIX = "saga:order:";
    private final KafkaProducer kafkaProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderService orderService;
    private final ObjectMapper mapper;

    // 트랜잭션이 커밋된 이후에 메서드를 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePendingOrderCreated(PendingOrderCreatedEvent event){
        OrderCreatedEvent orderEvent = createOrderEvent(event);
        kafkaProducer.sendMessage(ORDER_CREATED_TOPIC, orderEvent);
        savePendingOrder(event);
    }

    public void processSaga(String sagaKey){
        Map<Object, Object> sagaState = redisTemplate.opsForHash().entries(sagaKey);
        Set<String> requiredField = Set.of("product", "user", "coupon");
        Long orderId = (Long) sagaState.get("orderId");
        if(sagaState.keySet().containsAll(requiredField)){
            try {
                /* 주문 검증 수행 성공시
                    1.레디스 ZSET 데이터 삭제
                    2.레디스 HASH 데이터 삭제
                * */
                orderService.finalizeOrder(sagaState);
                clearCompleteOrder(orderId);
            } catch (OrderVerificationException e){
                /* 검증 실패시
                    1.레디스 ZSET 데이터 삭제
                    2.레디스 HASH 데이터 TTL 설정 (10분)
                 */
                initiateRollback(sagaState);
                clearFailureOrder(orderId);
            }
        }
    }

    private void clearFailureOrder(Long orderId){
        String sagaKey = HASH_PREFIX + orderId;
        redisTemplate.opsForZSet().remove(ZSET_PREFIX, orderId);
        redisTemplate.opsForHash().put(sagaKey, "status", "CANCELLED");
        redisTemplate.expire(sagaKey, 10, TimeUnit.MINUTES);
    }

    private void initiateRollback(Map<Object, Object> sagaState){
        if(sagaState.containsKey("product")){
            ProductStockDeductedEvent event = mapper.convertValue(sagaState.get("product"), ProductStockDeductedEvent.class);
            kafkaProducer.sendMessage(PRODUCT_ROLLBACK_TOPIC, event);
        }
        if(sagaState.containsKey("coupon")){
            CouponUsedSuccessEvent event = mapper.convertValue(sagaState.get("coupon"), CouponUsedSuccessEvent.class);
            kafkaProducer.sendMessage(COUPON_ROLLBACK_TOPIC, event);
        }
        if(sagaState.containsKey("user")){
            UserCacheDeductedEvent event = mapper.convertValue(sagaState.get("user"), UserCacheDeductedEvent.class);
            kafkaProducer.sendMessage(USER_ROLLBACK_TOPIC, event);
        }
    }

    private void clearCompleteOrder(Long orderId){
        redisTemplate.opsForZSet().remove(ZSET_PREFIX, orderId);
        redisTemplate.opsForHash().delete(HASH_PREFIX + orderId);
    }

    // redis Hash로 주문 생성 데이터를 저장 및 타임 아웃 시간 설정
    private void savePendingOrder(PendingOrderCreatedEvent event){
        addOrderData(event.getOrderId(), event.getStatus(), event.getCreatedAt().toString());
        addOrderTimeout(event.getOrderId(), 5, TimeUnit.MINUTES);
    }

    private void addOrderData(Long orderId, String status, String createdAt){
        String sagaKey = HASH_PREFIX + orderId;
        Map<String, Object> initialSagaState = new HashMap<>();
        initialSagaState.put("orderId", orderId);
        initialSagaState.put("status", status);
        initialSagaState.put("createdAt", createdAt);
        redisTemplate.opsForHash().putAll(sagaKey, initialSagaState);
    }

    private void addOrderTimeout(Long orderId, long timeout, TimeUnit timeUnit){
        long timeMilliSeconds = timeUnit.toMillis(timeout);
        double score = System.currentTimeMillis() + timeMilliSeconds;
        redisTemplate.opsForZSet().add(ZSET_PREFIX, orderId, score);
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
