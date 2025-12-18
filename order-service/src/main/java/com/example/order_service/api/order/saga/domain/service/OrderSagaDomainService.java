package com.example.order_service.api.order.saga.domain.service;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
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
    public SagaInstanceDto create(Long orderId, Payload payload){
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(orderId, payload);
        OrderSagaInstance savedSagaInstance = orderSagaInstanceRepository.save(sagaInstance);
        return SagaInstanceDto.from(savedSagaInstance);
    }

    public SagaInstanceDto getSaga(Long sagaId){
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        return SagaInstanceDto.from(sagaInstance);
    }

    //TODO 테스트 작성
    @Transactional
    public SagaInstanceDto nextStepToProduct(Long sagaId) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        sagaInstance.changeStep(SagaStep.PRODUCT);
        return SagaInstanceDto.from(sagaInstance);
    }

    @Transactional
    public SagaInstanceDto nextStepToCoupon(Long sagaId) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        sagaInstance.changeStep(SagaStep.COUPON);
        return SagaInstanceDto.from(sagaInstance);
    }

    @Transactional
    public SagaInstanceDto nextStepToUser(Long sagaId) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        sagaInstance.changeStep(SagaStep.USER);
        return SagaInstanceDto.from(sagaInstance);
    }

    @Transactional
    public SagaInstanceDto finish(Long sagaId) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        sagaInstance.changeStatus(SagaStatus.FINISHED);
        return SagaInstanceDto.from(sagaInstance);
    }

    //TODO 테스트 작성
    @Transactional
    public SagaInstanceDto fail(Long sagaId) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        sagaInstance.changeStatus(SagaStatus.FAILED);
        return SagaInstanceDto.from(sagaInstance);
    }

    public SagaInstanceDto abort(Long sagaId, String failureReason) {
        return null;
    }
}
