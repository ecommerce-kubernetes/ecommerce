package com.example.order_service.messaging;

import com.example.common.CouponUsedSuccessEvent;
import com.example.common.FailedEvent;
import com.example.common.ProductStockDeductedEvent;
import com.example.common.UserCacheDeductedEvent;
import com.example.order_service.service.SagaManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaListener {
    private final RedisTemplate<String, Object> redisTemplate;
    private final SagaManager sagaManager;

    private static final String PRODUCT_SUCCESS_TOPIC = "product.stock.deducted";
    private static final String COUPON_SUCCESS_TOPIC = "coupon.used.applied";
    private static final String USER_SUCCESS_TOPIC = "user.cache.deducted";
    private static final String USER_FAILURE_TOPIC = "user.cache.failed";

    @KafkaListener(topics = PRODUCT_SUCCESS_TOPIC)
    public void productSagaSuccessListener(@Payload ProductStockDeductedEvent event){
        String orderKey = "saga:order:" + event.getOrderId();
        if(redisTemplate.opsForHash().get(orderKey, "status") == null){
            //TODO 지각생 메시지 처리
            log.info("레디스 조회 결과 = null");
            return;
        }
        //레디스에 응답 데이터 저장
        redisTemplate.opsForHash().put(orderKey, "product", event);
        //사가 패턴 진행
        sagaManager.processSaga(orderKey);
    }

    @KafkaListener(topics = COUPON_SUCCESS_TOPIC)
    public void couponSagaSuccessListener(@Payload CouponUsedSuccessEvent event){
        String orderKey = "saga:order:" + event.getOrderId();
        if(redisTemplate.opsForHash().get(orderKey, "status") == null){
            //TODO 지각생 메시지 처리
            log.info("레디스 조회 결과 = null");
            return;
        }
        //레디스에 응답 데이터 저장
        redisTemplate.opsForHash().put(orderKey, "coupon", event);
        //사가 패턴 진행
        sagaManager.processSaga(orderKey);
    }

    @KafkaListener(topics = USER_SUCCESS_TOPIC)
    public void userSagaSuccessListener(@Payload UserCacheDeductedEvent event){
        String orderKey = "saga:order:" + event.getOrderId();
        if(redisTemplate.opsForHash().get(orderKey, "status") == null){
            //TODO 지각생 메시지 처리
            log.info("레디스 조회 결과 = null");
            return;
        }
        //레디스에 응답 데이터 저장
        redisTemplate.opsForHash().put(orderKey, "user", event);
        //사가 패턴 진행
        sagaManager.processSaga(orderKey);
    }

}
