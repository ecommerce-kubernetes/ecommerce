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

    @Value("${product-saga-command}")
    private String productCommandTopic;
    @Value("${coupon-saga-command}")
    private String couponCommandTopic;
    @Value("${user-saga-command}")
    private String userCommandTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestInventoryDeduction(Long sagaId, String orderNo, Payload payload) {
        ProductSagaCommand message = createInventoryRequestMessage(ProductCommandType.DEDUCT_STOCK, sagaId, orderNo, payload);
        kafkaTemplate.send(productCommandTopic, String.valueOf(sagaId), message);
    }

    public void requestCouponUse(Long sagaId, String orderNo, Payload payload) {
        CouponSagaCommand message = createCouponRequestMessage(CouponCommandType.USE_COUPON, sagaId, orderNo, payload);
        kafkaTemplate.send(couponCommandTopic, String.valueOf(sagaId), message);
    }

    public void requestUserPointUse(Long sagaId, String orderNo, Payload payload){
        UserSagaCommand message = createUserRequestMessage(UserCommandType.USE_POINT, sagaId, orderNo, payload);
        kafkaTemplate.send(userCommandTopic, String.valueOf(sagaId), message);
    }

    public void requestUserPointCompensate(Long sagaId, String orderNo, Payload payload) {
        UserSagaCommand message = createUserRequestMessage(UserCommandType.REFUND_POINT, sagaId, orderNo, payload);
        kafkaTemplate.send(userCommandTopic, String.valueOf(sagaId), message);
    }

    public void requestCouponCompensate(Long sagaId, String orderNo, Payload payload) {
        CouponSagaCommand message = createCouponRequestMessage(CouponCommandType.CANCEL_USE, sagaId, orderNo, payload);
        kafkaTemplate.send(couponCommandTopic, String.valueOf(sagaId), message);
    }

    public void requestInventoryCompensate(Long sagaId, String orderNo, Payload payload) {
        ProductSagaCommand message = createInventoryRequestMessage(ProductCommandType.RESTORE_STOCK, sagaId, orderNo, payload);
        kafkaTemplate.send(productCommandTopic, String.valueOf(sagaId), message);
    }

    private ProductSagaCommand createInventoryRequestMessage(ProductCommandType type, Long sagaId, String orderNo, Payload payload){
        LocalDateTime currentTimestamp = LocalDateTime.now();
        List<Item> items = payload.getSagaItems().stream()
                .map(item -> Item.of(item.getProductVariantId(), item.getQuantity()))
                .toList();

        return ProductSagaCommand.of(type, sagaId, orderNo, payload.getUserId(), items, currentTimestamp);
    }

    private CouponSagaCommand createCouponRequestMessage(CouponCommandType type, Long sagaId, String orderNo, Payload payload) {
        LocalDateTime currentTimestamp = LocalDateTime.now();
        return CouponSagaCommand.of(type, sagaId, orderNo, payload.getUserId(), payload.getCouponId(), currentTimestamp);
    }

    private UserSagaCommand createUserRequestMessage(UserCommandType type, Long sagaId, String orderNo, Payload payload){
        LocalDateTime currentTimestamp = LocalDateTime.now();
        return UserSagaCommand.of(type, sagaId, orderNo, payload.getUserId(), payload.getUseToPoint(), currentTimestamp);
    }
}
