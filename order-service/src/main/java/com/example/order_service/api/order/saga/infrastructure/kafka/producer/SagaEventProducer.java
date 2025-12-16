package com.example.order_service.api.order.saga.infrastructure.kafka.producer;

import com.example.common.InventoryDeductRequest;
import com.example.common.Item;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SagaEventProducer {

    @Value("${order.topics.deduct-inventory}")
    private String inventoryDeductedTopic;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestInventoryDeduction(Long sagaId, Long orderId, Payload payload) {
        InventoryDeductRequest message = createMessage(sagaId, orderId, payload);
        kafkaTemplate.send(inventoryDeductedTopic, String.valueOf(sagaId), message);
    }

    private InventoryDeductRequest createMessage(Long sagaId, Long orderId, Payload payload){
        LocalDateTime currentTimestamp = LocalDateTime.now();
        List<Item> items = payload.getSagaItems().stream()
                .map(item -> Item.of(item.getProductVariantId(), item.getQuantity()))
                .toList();

        return InventoryDeductRequest.of(sagaId, orderId, payload.getUserId(), items, currentTimestamp);
    }
}
