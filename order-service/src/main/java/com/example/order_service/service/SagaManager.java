package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.exception.OrderVerificationException;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import com.example.order_service.service.kafka.KafkaProducer;
import com.example.order_service.service.kafka.SagaCompensator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
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
    private final List<SagaCompensator> compensators;
    private Map<String, SagaCompensator> compensatorMap;
    private final static String ORDER_CREATED_TOPIC = "order.created";
    private final static String ZSET_PREFIX = "saga:timeouts";
    private final static String HASH_PREFIX = "saga:order:";
    private final KafkaProducer kafkaProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderService orderService;

    @PostConstruct
    public void initializeCompensators(){
        Map<String, SagaCompensator> map = new HashMap<>();
        for (SagaCompensator c : compensators) {
            String serviceName = c.getStepName();
            map.put(serviceName, c);
        }
        this.compensatorMap = map;
    }

    public void processPendingOrderSaga(PendingOrderCreatedEvent event){
        OrderCreatedEvent orderEvent = createOrderEvent(event);
        kafkaProducer.sendMessage(ORDER_CREATED_TOPIC, orderEvent);
        savePendingOrder(event);
    }

    //TODO race condition 고려
    //성공 응답시 호출
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
            // 응답이 모두 오지 않은 경우 스킵
        }
    }

    //TODO race condition 고려
    //실패 응답시 호출
    public void processSagaFailure(FailedEvent event){
        String sagaKey = HASH_PREFIX + event.getOrderId();
        String status = (String) redisTemplate.opsForHash().get(sagaKey, "status");
        if(status == null){
            //지각생 메시지
            return;
        }
        if(status.equals("CANCELLED")){
            //이미 실패된 주문에 대해 실패 응답이 온 경우
            return;
        }
        if(status.equals("PENDING")){
            //진행중인 주문에 대해 실패 응답이 온 경우
            //1. 주문 실패로 변경하기
            orderService.failOrder(event.getOrderId());
            //2. redis 에 저장된 모든 응답 꺼내기
            Map<Object, Object> sagaState = redisTemplate.opsForHash().entries(sagaKey);
            //3. redis ZSET 삭제, HASH TTL 설정
            clearFailureOrder(event.getOrderId(), sagaKey);
            initiateRollback(sagaState);
        }
    }

    public void processTimeoutFailure(Set<Long> orderIds){
        Map<Long, Map<Object, Object>> timeoutMap = new HashMap<>();
        for(Long orderId : orderIds){
            String sagaKey = HASH_PREFIX + orderId;
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(sagaKey);
            timeoutMap.put(orderId, entries);
        }
        timeoutMap.forEach((orderId, entries) -> {
            orderService.failOrder(orderId);
            clearFailureOrder(orderId, HASH_PREFIX + orderId);
            initiateRollback(entries);
        });
    }

    // HASH 에 들어있는 응답 모두 롤백
    private void initiateRollback(Map<Object, Object> sagaState){
        for (Map.Entry<Object, Object> entry : sagaState.entrySet()){
            String serviceName = String.valueOf(entry.getKey());
            SagaCompensator compensator = compensatorMap.get(serviceName);
            if(compensator != null){
                compensator.compensate(entry.getValue());
            }
        }
    }

    // 개별 롤백
    private void individualRollback(SuccessSagaEvent event){
        String serviceName = SAGA_STEP_FIELD.get(event.getClass());
        SagaCompensator compensator = compensatorMap.get(serviceName);
        if(compensator != null){
            compensator.compensate(event);
        }
    }

    // ZSET 삭제, HASH 삭제
    private void clearCompleteOrder(Long orderId, String sagaKey){
        redisTemplate.opsForZSet().remove(ZSET_PREFIX, orderId);
        redisTemplate.delete(sagaKey);
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
