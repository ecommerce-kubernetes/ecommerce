package com.example.order_service.api.order.saga.infrastructure;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import org.springframework.stereotype.Component;

@Component
public class SagaEventProducer {

    public void requestInventoryDeduction(Long sagaId, Payload payload) {

    }
}
