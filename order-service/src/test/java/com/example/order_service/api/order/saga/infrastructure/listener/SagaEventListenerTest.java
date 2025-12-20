package com.example.order_service.api.order.saga.infrastructure.listener;

import com.example.common.result.SagaProcessResult;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.support.IncludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class SagaEventListenerTest extends IncludeInfraTest {

    @MockitoBean
    private SagaManager sagaManager;

    @Test
    @DisplayName("상품 서비스 응답을 수신하면 sagaManager를 통해 Saga를 진행한다")
    void handleProductResult() {
        //given
        Long sagaId = 1L;
        SagaProcessResult result = SagaProcessResult.success(sagaId, 1L);
        //when
        kafkaTemplate.send(PRODUCT_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
        //then
        verify(sagaManager, timeout(10000).times(1)).processProductResult(refEq(result));
    }

    @Test
    @DisplayName("쿠폰 서비스 응답을 수신하면 sagaManager를 통해 Saga를 진행한다")
    void handleCouponResult() {
        //given
        Long sagaId = 1L;
        SagaProcessResult result = SagaProcessResult.success(sagaId, 1L);
        //when
        kafkaTemplate.send(COUPON_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
        //then
        verify(sagaManager, timeout(10000).times(1)).processCouponResult(refEq(result));
    }

    @Test
    @DisplayName("유저 서비스 응답을 수신하면 sagaManger를 통해 Saga를 진행한다")
    void handleUserResult(){
        //given
        Long sagaId = 1L;
        SagaProcessResult result = SagaProcessResult.success(sagaId, 1L);
        //when
        kafkaTemplate.send(USER_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
        //then
        verify(sagaManager, timeout(10000).times(1)).processUserResult(refEq(result));
    }
}
