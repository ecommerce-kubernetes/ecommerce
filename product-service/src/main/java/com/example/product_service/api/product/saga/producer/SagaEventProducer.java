package com.example.product_service.api.product.saga.producer;

import com.example.common.result.SagaProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventProducer {

    public void requestStockDeductionSuccess(Long sagaId, String orderNo) {
        SagaProcessResult result = SagaProcessResult.success(sagaId, orderNo);
    }


}
