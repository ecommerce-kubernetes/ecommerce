package com.example.order_service.api.order.saga.domain.service;

import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderSagaDomainService {

    private final OrderSagaInstanceRepository orderSagaInstanceRepository;

    @Transactional
    public SagaInstanceDto saveOrderSagaInstance(Long orderId, Payload payload){
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(orderId, payload);
        OrderSagaInstance savedSagaInstance = orderSagaInstanceRepository.save(sagaInstance);
        return SagaInstanceDto.from(savedSagaInstance);
    }

    public SagaInstanceDto getOrderSagaInstance(Long sagaId){
        return null;
    }

    public SagaInstanceDto updateToCouponSagaInstance(Long sagaId) {
        return null;
    }

    public SagaInstanceDto updateToPointSagaInstance(Long sagaId) {
        return null;
    }

    public SagaInstanceDto updateToCompleteSagaInstance(Long sagaId) {
        return null;
    }
}
