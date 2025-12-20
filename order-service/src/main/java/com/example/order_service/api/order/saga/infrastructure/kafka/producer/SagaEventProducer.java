package com.example.order_service.api.order.saga.infrastructure.kafka.producer;

import com.example.common.coupon.CouponCommandType;
import com.example.common.coupon.CouponSagaCommand;
import com.example.common.product.Item;
import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.common.user.UserCommandType;
import com.example.common.user.UserSagaCommand;
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

    @Value("${order.topics.product-request}")
    private String productRequestTopic;
    @Value("${order.topics.coupon-request}")
    private String couponRequestTopic;
    @Value("${order.topics.user-request}")
    private String userRequestTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestInventoryDeduction(Long sagaId, Long orderId, Payload payload) {
        ProductSagaCommand message = createInventoryRequestMessage(ProductCommandType.DEDUCT_STOCK, sagaId, orderId, payload);
        kafkaTemplate.send(productRequestTopic, String.valueOf(sagaId), message);
    }

    public void requestCouponUse(Long sagaId, Long orderId, Payload payload) {
        CouponSagaCommand message = createCouponRequestMessage(CouponCommandType.USE_COUPON, sagaId, orderId, payload);
        kafkaTemplate.send(couponRequestTopic, String.valueOf(sagaId), message);
    }

    public void requestUserPointUse(Long sagaId, Long orderId, Payload payload){
        UserSagaCommand message = createUserRequestMessage(UserCommandType.USE_POINT, sagaId, orderId, payload);
        kafkaTemplate.send(userRequestTopic, String.valueOf(sagaId), message);
    }

    public void requestUserPointCompensate(Long sagaId, Long orderId, Payload payload) {
        UserSagaCommand message = createUserRequestMessage(UserCommandType.REFUND_POINT, sagaId, orderId, payload);
        kafkaTemplate.send(userRequestTopic, String.valueOf(sagaId), message);
    }

    public void requestCouponCompensate(Long sagaId, Long orderId, Payload payload) {
        CouponSagaCommand message = createCouponRequestMessage(CouponCommandType.CANCEL_USE, sagaId, orderId, payload);
        kafkaTemplate.send(couponRequestTopic, String.valueOf(sagaId), message);
    }

    public void requestInventoryCompensate(Long sagaId, Long orderId, Payload payload) {
        ProductSagaCommand message = createInventoryRequestMessage(ProductCommandType.RESTORE_STOCK, sagaId, orderId, payload);
        kafkaTemplate.send(productRequestTopic, String.valueOf(sagaId), message);
    }

    private ProductSagaCommand createInventoryRequestMessage(ProductCommandType type, Long sagaId, Long orderId, Payload payload){
        LocalDateTime currentTimestamp = LocalDateTime.now();
        List<Item> items = payload.getSagaItems().stream()
                .map(item -> Item.of(item.getProductVariantId(), item.getQuantity()))
                .toList();

        return ProductSagaCommand.of(type, sagaId, orderId, payload.getUserId(), items, currentTimestamp);
    }

    private CouponSagaCommand createCouponRequestMessage(CouponCommandType type, Long sagaId, Long orderId, Payload payload) {
        LocalDateTime currentTimestamp = LocalDateTime.now();
        return CouponSagaCommand.of(type, sagaId, orderId, payload.getUserId(), payload.getCouponId(), currentTimestamp);
    }

    private UserSagaCommand createUserRequestMessage(UserCommandType type, Long sagaId, Long orderId, Payload payload){
        LocalDateTime currentTimestamp = LocalDateTime.now();
        return UserSagaCommand.of(type, sagaId, orderId, payload.getUserId(), payload.getUseToPoint(), currentTimestamp);
    }
}
