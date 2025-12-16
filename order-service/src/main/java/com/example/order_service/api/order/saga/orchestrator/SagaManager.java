package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SagaManager {

    private final OrderSagaDomainService orderSagaDomainService;
    private final SagaEventProducer sagaEventProducer;

    public void startSaga(SagaStartCommand command) {
        Payload payload = Payload.from(command);
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.saveOrderSagaInstance(command.getOrderId(), payload);
        sagaEventProducer.requestInventoryDeduction(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload());
    }

    public void proceedToCoupon(Long sagaId){
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.getOrderSagaInstance(sagaId);
        if(sagaInstanceDto.getPayload().getCouponId() == null) {
            proceedToUserPoint(sagaId);
            return;
        }

        SagaInstanceDto instance = orderSagaDomainService.updateToCouponSagaInstance(sagaId);
        sagaEventProducer.requestCouponUse(instance.getId(), instance.getOrderId(), instance.getPayload());
    }

    public void proceedToUserPoint(Long sagaId) {
        SagaInstanceDto orderSagaInstance = orderSagaDomainService.getOrderSagaInstance(sagaId);
        Long useToPoint = orderSagaInstance.getPayload().getUseToPoint();
        if (useToPoint == null || useToPoint == 0) {
            //TODO 사가 종료
        }

        SagaInstanceDto instance = orderSagaDomainService.updateToPointSagaInstance(sagaId);
        sagaEventProducer.requestUserPointUse(instance.getId(), instance.getOrderId(), instance.getPayload());
    }

    public void abortSaga(Long sagaId, String failureReason){

    }
}
