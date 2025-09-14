package com.example.order_service.messaging;

import com.example.common.ProductStockDeductedEvent;
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

    @KafkaListener(topics = PRODUCT_SUCCESS_TOPIC)
    public void productSagaListener(@Payload ProductStockDeductedEvent event){
        String orderKey = "saga:order:" + event.getOrderId();
        if(redisTemplate.opsForHash().get(orderKey, "status") == null){
            //TODO 지각생 메시지 처리
            log.info("레디스 조회 결과 = null");
            return;
        }

        redisTemplate.opsForHash().put(orderKey, "product", event);
        sagaManager.processSaga(orderKey);
    }
}
