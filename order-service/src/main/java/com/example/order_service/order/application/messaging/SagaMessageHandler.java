package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.domain.saga.SagaStep;

/**
 * SAGA 메시지 핸들러 인터페이스
 * <p>
 * 처리 가능 Step 및 차감, 보상 메서드
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 08
 */
public interface SagaMessageHandler {
    /**
     * 처리 가능 차감 Step
     * @return SAGA 스텝
     */
    SagaStep supportsForward();

    /**
     * 보상 가능 Step
     * @return SAGA 스텝
     */
    SagaStep supportsCompensation();

    /**
     * 보상 로직
     * @param message SAGA 메시지
     */
    void compensate(SagaMessage message);

    /**
     * 차감 로직
     * @param message SAGA 메시지
     */
    void forward(SagaMessage message);
}
