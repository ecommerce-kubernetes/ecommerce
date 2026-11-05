package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import com.example.order_service.order.config.OrderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExpirationProcessor {
    private final OrderProperties orderProperties;
    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    public void processTimeoutOrders(LocalDateTime currentTime) {
        LocalDateTime timeoutThreshold = currentTime.minusMinutes(orderProperties.timeoutMinute());
        List<OrderSummaryResult> timeoutOrders = orderQueryService.getOrdersByPendingAndCreatedAtBefore(timeoutThreshold);
        if (timeoutOrders.isEmpty()) {
            return;
        }

        log.info("[OrderTimeoutScheduler] 타임아웃 대상 주문 건수: {}", timeoutOrders.size());

        for (OrderSummaryResult order : timeoutOrders) {
            try {
                orderCommandService.changeFailed(order.orderId(), "접수 시간 초과");
            } catch (Exception e) {
                log.error("[OrderTimeoutScheduler] 주문 취소 처리 실패 - orderId: {}", order.orderId(), e);
            }
        }
    }
}
