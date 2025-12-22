package com.example.order_service.api.order.saga.scheduler;

import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaTimeoutScheduler {
    private final SagaManager sagaManager;

    @Scheduled(fixedDelay = 60000)
    public void checkTimeouts() {
        sagaManager.processTimeouts();
    }
}
