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
    private static final Map<Class<? extends SuccessSagaEvent>, String> SAGA_STEP_FIELD = Map.of(
            ProductStockDeductedEvent.class, "product",
            CouponUsedSuccessEvent.class, "coupon",
            UserCashDeductedEvent.class, "user"
    );
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

    public void processSagaSuccess(SuccessSagaEvent event){
        String sagaKey = HASH_PREFIX + event.getOrderId();
        String status = (String) redisTemplate.opsForHash().get(sagaKey, "status");
        if(status == null) {
            //지각생 메시지
            individualRollback(event);
            return;
        }
        if(status.equals("CANCELLED")){
            //이미 실패된 주문에 성공 응답이 온 경우
            individualRollback(event);
            return;
        }
        if(status.equals("PENDING")){
            //주문이 아직 진행중인 경우
            //1. 응답 저장
            String field = SAGA_STEP_FIELD.get(event.getClass());
            redisTemplate.opsForHash().put(sagaKey, field, event);
            //2. 저장된 모든 응답 꺼내기
            Map<Object, Object> sagaState = redisTemplate.opsForHash().entries(sagaKey);
            Set<String> requiredField = Set.copyOf(SAGA_STEP_FIELD.values());
            // 응답이 모두 온 경우
            if(sagaState.keySet().containsAll(requiredField)){
                try{
                    //검증이 성공하면 주문 데이터를 저장하고 레디스 ZSET, HASH 모두 삭제
                    orderService.finalizeOrder(sagaState);
                    clearCompleteOrder(event.getOrderId(), sagaKey);
                } catch (OrderVerificationException e){
                    //검증이 실패하면 주문 데이터를 저장하고 레디스 ZSET 삭제, HASH TTL 설정
                    clearFailureOrder(event.getOrderId(), sagaKey);
                    initiateRollback(sagaState);
                }
            }
        }
    }

    // HASH 에 들어있는 응답 모두 롤백
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
            UserCashDeductedEvent event = mapper.convertValue(sagaState.get("user"), UserCashDeductedEvent.class);
            kafkaProducer.sendMessage(USER_ROLLBACK_TOPIC, event);
        }
    }

    // 개별 롤백
    private void individualRollback(SuccessSagaEvent event){
        String service = SAGA_STEP_FIELD.get(event.getClass());
        if (service.equals("product")){
            kafkaProducer.sendMessage(PRODUCT_ROLLBACK_TOPIC, event);
            return;
        }
        if (service.equals("coupon")){
            kafkaProducer.sendMessage(COUPON_ROLLBACK_TOPIC, event);
            return;
        }
        if (service.equals("user")){
            kafkaProducer.sendMessage(USER_ROLLBACK_TOPIC, event);
        }
    }

    // ZSET 삭제, HASH 삭제
    private void clearCompleteOrder(Long orderId, String sagaKey){
        redisTemplate.opsForZSet().remove(ZSET_PREFIX, orderId);
        redisTemplate.opsForHash().delete(sagaKey);
    }

    // ZSET 삭제, HASH 상태 CANCELLED 변경, HASH TTL 설정
    private void clearFailureOrder(Long orderId, String sagaKey){
        redisTemplate.opsForZSet().remove(ZSET_PREFIX, orderId);
        redisTemplate.opsForHash().put(sagaKey, "status", "CANCELLED");
        redisTemplate.expire(sagaKey, 10, TimeUnit.MINUTES);
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
