package com.example.order_service.order.application.service.saga;

import com.example.order_service.order.application.orchestrator.OrderSagaManager;
import com.example.order_service.order.application.service.saga.dto.OrderSagaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCleanupProcessor {

    private static final int CHUNK_SIZE = 20;
    private static final long THROTTLE_MS = 100L;
    private static final int THRESHOLD_MINUTES = 5;

    private final OrderSagaService orderSagaService;
    private final OrderSagaManager orderSagaManager;
    private final Clock clock;

    public void cleanupTimeoutSaga() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(THRESHOLD_MINUTES);
        List<OrderSagaResult.Default> sagas = orderSagaService.getSagasBefore(threshold, CHUNK_SIZE);
        if (sagas.isEmpty()) {
            return;
        }

        for (OrderSagaResult.Default saga : sagas) {
            try {
                orderSagaManager.timeoutSaga(saga.sagaId());
                Thread.sleep(THROTTLE_MS);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.info("[SAGA 스케줄링] 타임아웃 처리 중 응답 수신 확인 (낙관적 락). 타임아웃 무시 sagaId = {}", saga.sagaId());
            } catch (InterruptedException e) {
                log.info("[SAGA 스케줄링 조기 종료]");
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("[SAGA 스케줄링] 내부 시스템 에러. 다음 스케줄러 대기 sagaId = {}", saga.sagaId(), e);
            }
        }
    }
}
