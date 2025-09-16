package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.dto.request.OrderRequest;
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
import java.util.List;
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
    private final ObjectMapper mapper = new ObjectMapper();

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
            ProductStockDeductedEvent productEvent = mapper.convertValue(sagaState.get("product"), ProductStockDeductedEvent.class);
            UserCacheDeductedEvent userEvent = mapper.convertValue(sagaState.get("user"), UserCacheDeductedEvent.class);
            CouponUsedSuccessEvent couponEvent = mapper.convertValue(sagaState.get("coupon"), CouponUsedSuccessEvent.class);
            boolean isVerifyPayment = verifyPayment(productEvent, userEvent, couponEvent);
            if(isVerifyPayment){
                //TODO 주문 상태 변경, 주문 데이터 추가, SSE 연결 찾아 응답 반환
            } else {
                //TODO 롤백
            }
        }
    }

    private boolean verifyPayment(ProductStockDeductedEvent productEvent,
                                  UserCacheDeductedEvent userEvent,
                                  CouponUsedSuccessEvent couponEvent){

        long itemsPrice = calcOrderItemsTotalPrice(productEvent);
        //최소 결제 가격 만족 여부
        if(couponEvent.getMinPurchaseAmount() > itemsPrice){
            return false;
        }
        long couponDiscount = calcCouponDiscount(couponEvent, itemsPrice);
        // 최대 할인금액보다 할인 금액이 높으면 최대 할인 금액을 적용
        if(couponDiscount > couponEvent.getMaxDiscountAmount()){
            couponDiscount = couponEvent.getMaxDiscountAmount();
        }
        // 검증 수행
        return userEvent.getExpectTotalAmount() == itemsPrice - couponDiscount;
    }

    private long calcOrderItemsTotalPrice(ProductStockDeductedEvent event){
        return event.getDeductedProducts().stream()
                .mapToLong(p -> p.getPriceInfo().getFinalPrice() * p.getQuantity())
                .sum();
    }

    private long calcCouponDiscount(CouponUsedSuccessEvent event, long itemsPrice){
        if(event.getDiscountType() == DiscountType.AMOUNT){
            return event.getDiscountValue();
        }

        return (long) (itemsPrice * (event.getDiscountValue() / 100.0));
    }


    // redis Hash로 주문 생성 데이터를 저장 및 타임 아웃 시간 설정
    private void storeOrderDataToRedis(PendingOrderCreatedEvent event){
        String sagaKey = "saga:order:" + event.getOrderId();

        Map<String, Object> initialSagaState = new HashMap<>();

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
