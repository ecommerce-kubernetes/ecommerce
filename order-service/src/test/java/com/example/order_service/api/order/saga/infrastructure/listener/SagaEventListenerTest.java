package com.example.order_service.api.order.saga.infrastructure.listener;

import com.example.common.result.SagaProcessResult;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStepResultCommand;
import com.example.order_service.api.support.IncludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class SagaEventListenerTest extends IncludeInfraTest {

    @MockitoBean
    private SagaManager sagaManager;

    private static final String ORDER_NO = "ORD-20260101-AB12FVC";
    @Captor
    private ArgumentCaptor<SagaStepResultCommand> sagaCommandCaptor;

    @Nested
    @DisplayName("상품 서비스 응답 수신")
    class HandleProductResult {

        @Test
        @DisplayName("상품 성공 이벤트를 수신하면 sagaManager를 통해 Saga를 진행한다")
        void handleProductResult_success() {
            //given
            Long sagaId = 1L;
            SagaProcessResult result = SagaProcessResult.success(sagaId, ORDER_NO);
            //when
            kafkaTemplate.send(PRODUCT_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
            //then
            verify(sagaManager, timeout(1000)).handleStepResult(sagaCommandCaptor.capture());

            assertThat(sagaCommandCaptor.getValue())
                    .extracting(SagaStepResultCommand::getStep, SagaStepResultCommand::getOrderNo,
                            SagaStepResultCommand::isSuccess, SagaStepResultCommand::getErrorCode, SagaStepResultCommand::getFailureReason)
                    .containsExactly(SagaStep.PRODUCT, ORDER_NO, true, null, null);
        }

        @Test
        @DisplayName("상품 실패 이벤트를 수신하면 sagaManager를 통해 Saga를 진행한다")
        void handleProductResult_fail() {
            Long sagaId = 1L;
            SagaProcessResult result = SagaProcessResult.fail(sagaId, ORDER_NO, "INSUFFICIENT_STOCK", "상품 재고가 부족합니다");
            //when
            kafkaTemplate.send(PRODUCT_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
            //then
            verify(sagaManager, timeout(1000)).handleStepResult(sagaCommandCaptor.capture());

            assertThat(sagaCommandCaptor.getValue())
                    .extracting(SagaStepResultCommand::getStep, SagaStepResultCommand::getOrderNo,
                            SagaStepResultCommand::isSuccess, SagaStepResultCommand::getErrorCode, SagaStepResultCommand::getFailureReason)
                    .containsExactly(SagaStep.PRODUCT, ORDER_NO, false, "INSUFFICIENT_STOCK", "상품 재고가 부족합니다");
        }
    }

    @Nested
    @DisplayName("쿠폰 서비스 응답 수신")
    class HandleCouponResult {

        @Test
        @DisplayName("쿠폰 성공 이벤트를 수신하면 sagaManager를 통해 Saga를 진행한다")
        void handleCouponResult_success() {
            //given
            Long sagaId = 1L;
            SagaProcessResult result = SagaProcessResult.success(sagaId, ORDER_NO);
            //when
            kafkaTemplate.send(COUPON_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
            //then
            verify(sagaManager, timeout(1000)).handleStepResult(sagaCommandCaptor.capture());

            assertThat(sagaCommandCaptor.getValue())
                    .extracting(SagaStepResultCommand::getStep, SagaStepResultCommand::getOrderNo,
                            SagaStepResultCommand::isSuccess, SagaStepResultCommand::getErrorCode, SagaStepResultCommand::getFailureReason)
                    .containsExactly(SagaStep.COUPON, ORDER_NO, true, null, null);
        }

        @Test
        @DisplayName("쿠폰 실패 이벤트를 수신하면 sagaManager를 통해 Saga를 진행한다")
        void handleCouponResult_fail() {
            Long sagaId = 1L;
            SagaProcessResult result = SagaProcessResult.fail(sagaId, ORDER_NO, "INVALID_COUPON", "유효하지 않은 쿠폰입니다");
            //when
            kafkaTemplate.send(COUPON_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
            //then
            verify(sagaManager, timeout(1000)).handleStepResult(sagaCommandCaptor.capture());

            assertThat(sagaCommandCaptor.getValue())
                    .extracting(SagaStepResultCommand::getStep, SagaStepResultCommand::getOrderNo,
                            SagaStepResultCommand::isSuccess, SagaStepResultCommand::getErrorCode, SagaStepResultCommand::getFailureReason)
                    .containsExactly(SagaStep.COUPON, ORDER_NO, false, "INVALID_COUPON", "유효하지 않은 쿠폰입니다");
        }
    }

    @Nested
    @DisplayName("유저 서비스 응답 수신")
    class HandleUserResult {
        @Test
        @DisplayName("유저 성공 이벤트를 수신하면 sagaManager를 통해 Saga를 진행한다")
        void handleUserResult_success() {
            //given
            Long sagaId = 1L;
            SagaProcessResult result = SagaProcessResult.success(sagaId, ORDER_NO);
            //when
            kafkaTemplate.send(USER_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
            //then
            verify(sagaManager, timeout(1000)).handleStepResult(sagaCommandCaptor.capture());

            assertThat(sagaCommandCaptor.getValue())
                    .extracting(SagaStepResultCommand::getStep, SagaStepResultCommand::getOrderNo,
                            SagaStepResultCommand::isSuccess, SagaStepResultCommand::getErrorCode, SagaStepResultCommand::getFailureReason)
                    .containsExactly(SagaStep.USER, ORDER_NO, true, null, null);
        }

        @Test
        @DisplayName("유저 실패 이벤트를 수신하면 sagaManager를 통해 Saga를 진행한다")
        void handleUserResult_fail() {
            Long sagaId = 1L;
            SagaProcessResult result = SagaProcessResult.fail(sagaId, ORDER_NO, "INSUFFICIENT_POINT", "포인트가 부족합니다");
            //when
            kafkaTemplate.send(USER_RESULT_TOPIC_NAME, String.valueOf(result.getSagaId()), result);
            //then
            verify(sagaManager, timeout(1000)).handleStepResult(sagaCommandCaptor.capture());

            assertThat(sagaCommandCaptor.getValue())
                    .extracting(SagaStepResultCommand::getStep, SagaStepResultCommand::getOrderNo,
                            SagaStepResultCommand::isSuccess, SagaStepResultCommand::getErrorCode, SagaStepResultCommand::getFailureReason)
                    .containsExactly(SagaStep.USER, ORDER_NO, false, "INSUFFICIENT_POINT", "포인트가 부족합니다");
        }
    }
}
