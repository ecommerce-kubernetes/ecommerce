package com.example.order_service.service.kafka;

import com.example.common.ProductStockDeductedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCompensator implements SagaCompensator {
    private final ObjectMapper mapper;
    private final KafkaProducer kafkaProducer;
    private static final String PRODUCT_ROLLBACK_TOPIC = "product.stock.restore";
    @Override
    public String getStepName() {
        return "product";
    }

    @Override
    public void compensate(Object rollbackEvent) {
        ProductStockDeductedEvent event = mapper.convertValue(rollbackEvent, ProductStockDeductedEvent.class);
        kafkaProducer.sendMessage(PRODUCT_ROLLBACK_TOPIC, event);
    }
}
