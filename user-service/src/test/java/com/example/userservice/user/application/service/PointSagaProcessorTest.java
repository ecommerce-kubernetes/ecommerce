package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.user.application.port.UserOutboxPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PointSagaProcessorTest {

    @InjectMocks
    private PointSagaProcessor pointSagaProcessor;

    @Mock
    private PointCommandService pointCommandService;
    @Mock
    private UserOutboxPort userOutboxPort;

    @Test
    @DisplayName("포인트를 차감하고 아웃박스에 정방향 성공을 기록한다.")
    void deduct() {
        //given
        Long sagaId = 1L;
        Long executionId = 2L;
        Long userId = 3L;
        Money amount = Money.wons(1000L);
        //when
        pointSagaProcessor.deduct(sagaId, executionId, userId, amount);
        //then
        then(pointCommandService).should().deductPoint(userId, executionId, amount);
        then(userOutboxPort).should().recordForwardSuccess(sagaId, executionId);
    }

    @Test
    @DisplayName("포인트를 환불하고 아웃박스에 보상 성공을 기록한다.")
    void refund() {
        //given
        Long sagaId = 1L;
        Long executionId = 2L;
        Long userId = 3L;
        Money amount = Money.wons(1000L);
        //when
        pointSagaProcessor.refund(sagaId, executionId, userId, amount);
        //then
        then(pointCommandService).should().addPoint(userId, executionId, amount);
        then(userOutboxPort).should().recordCompensateSuccess(sagaId, executionId);
    }

    @Test
    @DisplayName("포인트 차감 실패를 아웃박스에 정방향 실패로 기록한다.")
    void failDeduct() {
        //given
        Long sagaId = 1L;
        Long executionId = 2L;
        String reason = "포인트 부족";
        //when
        pointSagaProcessor.failDeduct(sagaId, executionId, reason);
        //then
        then(userOutboxPort).should().recordForwardFail(sagaId, executionId, reason);
    }

    @Test
    @DisplayName("포인트 환불 실패를 아웃박스에 보상 실패로 기록한다.")
    void failRefund() {
        //given
        Long sagaId = 1L;
        Long executionId = 2L;
        String reason = "환불 실패";
        //when
        pointSagaProcessor.failRefund(sagaId, executionId, reason);
        //then
        then(userOutboxPort).should().recordCompensateFail(sagaId, executionId, reason);
    }
}
