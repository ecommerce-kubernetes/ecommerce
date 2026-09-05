package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCleanupProcessor {
    private final OrderQueryService queryService;
    private final OrderCommandService commandService;
    private final Clock clock;

    private static final int THRESHOLD_MINUTES = 30;
    private static final int CHUNK_SIZE = 100;
    private static final long THROTTLE_MS = 50L;

    public void cleanupExpiredPendingOrders() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(THRESHOLD_MINUTES);
        List<OrderResult.Summary> orders = queryService.getPendingOrdersBefore(threshold, CHUNK_SIZE);
        if (orders.isEmpty()) {
            return;
        }
        for (OrderResult.Summary order : orders) {
            try {
                commandService.changeFailed(order.orderNo(), "SYSTEM_TIMEOUT");
                Thread.sleep(THROTTLE_MS);
            } catch (InterruptedException e) {
                log.info("[대기 주문 취소 스케줄링] 조기 종료");
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("[대기 주문 취소 스케줄링] 내부 시스템 오류 발생 orderNo = {}", order.orderNo(), e);
            }
        }
    }
}
