package com.example.order_service.api.order.saga.infrastructure.kafka.producer;

import com.example.common.CouponUseRequest;
import com.example.common.InventoryDeductRequest;
import com.example.common.Item;
import com.example.common.UserPointUseRequest;
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
    @Value("${order.topics.used-coupon}")
    private String couponUsedTopic;
    @Value("${order.topics.used-point}")
    private String pointUsedTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestInventoryDeduction(Long sagaId, Long orderId, Payload payload) {
        InventoryDeductRequest message = createInventoryDeductMessage(sagaId, orderId, payload);
        kafkaTemplate.send(inventoryDeductedTopic, String.valueOf(sagaId), message);
    }

    public void requestCouponUse(Long sagaId, Long orderId, Payload payload) {
        CouponUseRequest message = createCouponUseMessage(sagaId, orderId, payload);
        kafkaTemplate.send(couponUsedTopic, String.valueOf(sagaId), message);
    }

    public void requestUserPointUse(Long sagaId, Long orderId, Payload payload){
        UserPointUseRequest message = createUsePointMessage(sagaId, orderId, payload);
        kafkaTemplate.send(pointUsedTopic, String.valueOf(sagaId), message);
    }

    private InventoryDeductRequest createInventoryDeductMessage(Long sagaId, Long orderId, Payload payload){
        LocalDateTime currentTimestamp = LocalDateTime.now();
        List<Item> items = payload.getSagaItems().stream()
                .map(item -> Item.of(item.getProductVariantId(), item.getQuantity()))
                .toList();

        return InventoryDeductRequest.of(sagaId, orderId, payload.getUserId(), items, currentTimestamp);
    }

    private CouponUseRequest createCouponUseMessage(Long sagaId, Long orderId, Payload payload) {
        LocalDateTime currentTimestamp = LocalDateTime.now();
        return CouponUseRequest.of(sagaId, orderId, payload.getUserId(), payload.getCouponId(), currentTimestamp);
    }

    private UserPointUseRequest createUsePointMessage(Long sagaId, Long orderId, Payload payload){
        LocalDateTime currentTimestamp = LocalDateTime.now();
        return UserPointUseRequest.of(sagaId, orderId, payload.getUserId(), payload.getUseToPoint(), "ORDER_DISCOUNT", currentTimestamp);
    }
}
