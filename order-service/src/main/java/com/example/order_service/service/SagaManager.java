package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import com.example.order_service.service.kafka.KafkaProducer;
import com.example.order_service.service.kafka.SagaCompensator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
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
    private final static String SET_PREFIX = "saga:steps:";
    private final KafkaProducer kafkaProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderService orderService;
    private final RedisScript<Long> checkAndCompleteSagaScript;
    private final RedisScript<Long> processSagaFailureScript;
    private final ObjectMapper mapper;

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
        kafkaProducer.sendMessage(ORDER_CREATED_TOPIC, String.valueOf(event.getOrderId()), orderEvent);
        savePendingOrder(event);
    }

    //TODO race condition 고려
    //성공 응답시 호출
    public void processSagaSuccess(SuccessSagaEvent event){
        String sagaKey = HASH_PREFIX + event.getOrderId();
        String stepKey = SET_PREFIX + event.getOrderId();
        String field = SAGA_STEP_FIELD.get(event.getClass());
        String eventJson = parseToJson(event);
        Long remainingSteps = redisTemplate
                .execute(checkAndCompleteSagaScript, Arrays.asList(sagaKey, stepKey), field, eventJson);
        log.info("success {} redis remainingStep = {}", event.getClass(), remainingSteps);
        if(remainingSteps == 0){
            orderService.completeOrder(event.getOrderId());
            clearCompleteOrder(event.getOrderId(), sagaKey);
        } else if (remainingSteps == -1){
            individualRollback(event);
        }
    }
    //실패 응답시 호출
    public void processSagaFailure(FailedEvent event){
        String sagaKey = HASH_PREFIX + event.getOrderId();
        String stepKey = SET_PREFIX + event.getOrderId();
        Long orderId = event.getOrderId();
        long ttlInSecond = 600;
        log.info("failure");
        Long result = redisTemplate.execute(processSagaFailureScript,
                Arrays.asList(sagaKey, ZSET_PREFIX, stepKey),
                String.valueOf(orderId), String.valueOf(ttlInSecond));
        log.info("redis result= {}", result);
        if (result == 1){
            log.info("order cancelled");
            orderService.cancelOrder(orderId);
            Map<Object, Object> sagaState = redisTemplate.opsForHash().entries(sagaKey);
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
            orderService.cancelOrder(orderId);
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
        redisTemplate.opsForZSet().remove(ZSET_PREFIX, String.valueOf(orderId));
        redisTemplate.delete(SET_PREFIX + orderId);
        redisTemplate.delete(sagaKey);
    }

    // ZSET 삭제, HASH 상태 CANCELLED 변경, HASH TTL 설정
    private void clearFailureOrder(Long orderId, String sagaKey){
        redisTemplate.opsForZSet().remove(ZSET_PREFIX, String.valueOf(orderId));
        redisTemplate.delete(SET_PREFIX + orderId);
        redisTemplate.opsForHash().put(sagaKey, "status", "CANCELLED");
        redisTemplate.expire(sagaKey, 10, TimeUnit.MINUTES);
    }

    // redis Hash로 주문 생성 데이터를 저장 및 타임 아웃 시간 설정
    private void savePendingOrder(PendingOrderCreatedEvent event){
        addOrderData(event.getOrderId(), event.getStatus(), event.getCreatedAt().toString());
        addRequiredField(event.getOrderId(), (event.getCouponId() != null));
        addOrderTimeout(event.getOrderId(), 5, TimeUnit.MINUTES);
    }

    private void addRequiredField(Long orderId, boolean couponUsage){
        String stepKey = SET_PREFIX + orderId;
        List<String> requiredField = new ArrayList<>();
        requiredField.add("product");
        requiredField.add("user");
        if(couponUsage){
            requiredField.add("coupon");
        }
        redisTemplate.opsForSet().add(stepKey, requiredField.toArray(new String[0]));
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
        redisTemplate.opsForZSet().add(ZSET_PREFIX, String.valueOf(orderId), score);
    }

    private OrderCreatedEvent createOrderEvent(PendingOrderCreatedEvent event){
        Map<Long, Integer> itemsMap = event.getVariantIdQuantiyMap();
        for (Long l : itemsMap.keySet()) {
            log.info("variant Id = {}" , l);
        }
        List<DeductedProduct> orderProducts = itemsMap.keySet().stream().map(variantId -> new DeductedProduct(variantId, itemsMap.get(variantId))).toList();
        return new OrderCreatedEvent(event.getOrderId(), event.getUserId(), event.getCouponId(), orderProducts,
                (event.getUsedPoint() != 0),
                event.getUsedPoint(),
                event.getAmountToPay(),
                event.getUsedPoint() + event.getAmountToPay());
    }

    private String parseToJson(Object o){
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e){
            throw new RuntimeException("Json Parse Exception",e);
        }
    }
}
