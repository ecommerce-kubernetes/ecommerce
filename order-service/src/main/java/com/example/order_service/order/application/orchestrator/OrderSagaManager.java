package com.example.order_service.order.application.orchestrator;

import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.application.service.saga.OrderSagaService;
import com.example.order_service.order.application.service.saga.dto.OrderSagaCommand;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderSagaManager {

    private final OrderCommandService orderCommandService;
    private final OrderSagaService orderSagaService;

    @Transactional
    public void startSaga(String orderNo) {
        OrderResult.Detail order = orderCommandService.changePaid(orderNo);
        SagaPayload payload = createPayload(order);
        OrderSagaCommand.Create command = OrderSagaCommand.Create.of(order.orderNo(), SagaStep.INVENTORY_DEDUCT_PENDING,
                SagaStatus.STARTED, payload);
        orderSagaService.createSaga(command);
    }

    private SagaPayload createPayload(OrderResult.Detail order) {
        List<Long> itemCouponIds = order.items().stream()
                .map(item -> item.itemCoupon().getCouponId())
                .filter(java.util.Objects::nonNull)
                .toList();
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(order.usedPoints());
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(order.cartCoupon().getCouponId(), itemCouponIds);
        List<SagaPayload.ItemPayload> itemPayloads = order.items().stream().map(item -> SagaPayload.ItemPayload
                .of(item.product().getProductVariantId(), item.quantity())).toList();
        return SagaPayload.of(order.orderer().getUserId(), itemPayloads, couponPayload, pointPayload);
    }
}
