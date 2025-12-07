package com.example.order_service.api.common.scheduler;

import com.example.order_service.service.SagaManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PendingOrderTimeoutScheduler {
    private final static String ZSET_PREFIX = "saga:timeouts";
    private final SagaManager sagaManager;

    private final RedisTemplate<String, Object> redisTemplate;
    @Scheduled(fixedRate = 60000)
    public void processTimeoutPendingOrder(){
        Set<Long> timeoutOrderIds = findExpiredPendingOrder();
        sagaManager.processTimeoutFailure(timeoutOrderIds);
    }

    private Set<Long> findExpiredPendingOrder(){
        long nowMillis = System.currentTimeMillis();
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        Set<Object> expiredOrder = zSetOps.rangeByScore(ZSET_PREFIX, 0, nowMillis);
        if(expiredOrder == null || expiredOrder.isEmpty()){
            return Collections.emptySet();
        }

        return expiredOrder.stream()
                .map(obj -> Long.parseLong(String.valueOf(obj)))
                .collect(Collectors.toSet());
    }
}
