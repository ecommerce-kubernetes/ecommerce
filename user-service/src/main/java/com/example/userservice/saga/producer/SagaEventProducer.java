package com.example.userservice.saga.producer;

import com.example.common.result.SagaProcessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventProducer {

    public void sendSagaSuccess(Long sagaId, String orderNo) {
        SagaProcessResult result = SagaProcessResult.success(sagaId, orderNo);
    }

    public void sendSagaFailure(Long sagaId, String orderNo, String errorCode, String failureReason) {
        SagaProcessResult result = SagaProcessResult.fail(sagaId, orderNo, errorCode, failureReason);
    }
}
