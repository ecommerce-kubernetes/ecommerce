package com.example.order_service.api.order.saga.infrastructure.listener;

import com.example.common.SagaProcessResult;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.support.IncludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class SagaEventListenerTest extends IncludeInfraTest {

    @MockitoBean
    private SagaManager sagaManager;

    @Test
    @DisplayName("상품 재고감소 응답의 SagaStatus가 SUCCESS 면 쿠폰 saga를 진행한다")
    void handleProductResult_SUCCESS() {
        //given
        Long sagaId = 1L;
        SagaProcessResult result = SagaProcessResult.success(sagaId, 1L);
        //when
        kafkaTemplate.send(PRODUCT_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
        //then
        verify(sagaManager, timeout(3000).times(1)).proceedSaga(sagaId);
    }

    @Test
    @DisplayName("상품 재고감소 응답의 SagaStatus가 FAIL 이면 보상 로직을 수행한다")
    void handleProductResult_FAIL() {
        //given
        Long sagaId = 1L;
        SagaProcessResult result = SagaProcessResult.fail(sagaId, 1L, "OUT_OF_STOCK", "재고가 부족합니다");
        //when
        kafkaTemplate.send(PRODUCT_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
        //then
        verify(sagaManager, timeout(3000).times(1)).abortSaga(sagaId, "재고가 부족합니다");
    }
}
