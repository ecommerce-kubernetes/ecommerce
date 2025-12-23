package com.example.order_service.api.order.saga.domain.service;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderSagaDomainService {

    private final OrderSagaInstanceRepository orderSagaInstanceRepository;

    public SagaInstanceDto create(Long orderId, Payload payload, SagaStep firstStep){
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(orderId, payload, firstStep);
        OrderSagaInstance savedSagaInstance = orderSagaInstanceRepository.save(sagaInstance);
        return SagaInstanceDto.from(savedSagaInstance);
    }

    @Transactional(readOnly = true)
    public SagaInstanceDto getSaga(Long sagaId){
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        return SagaInstanceDto.from(sagaInstance);
    }

    public SagaInstanceDto proceedTo(Long sagaId, SagaStep sagaStep) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));

        sagaInstance.proceedTo(sagaStep);
        return SagaInstanceDto.from(sagaInstance);
    }

    public SagaInstanceDto finish(Long sagaId) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        sagaInstance.changeStatus(SagaStatus.FINISHED);
        return SagaInstanceDto.from(sagaInstance);
    }

    public SagaInstanceDto fail(Long sagaId, String failureReason) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        sagaInstance.fail(failureReason);
        return SagaInstanceDto.from(sagaInstance);
    }

    public SagaInstanceDto startCompensation(Long sagaId, SagaStep nextStep, String failureReason) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));

        if (sagaInstance.getSagaStatus() != SagaStatus.STARTED) {
            log.info("이미 처리된 Saga 인스턴스");
            return SagaInstanceDto.from(sagaInstance);
        }

        sagaInstance.startCompensation(nextStep, failureReason);
        return SagaInstanceDto.from(sagaInstance);
    }

    public SagaInstanceDto continueCompensation(Long sagaId, SagaStep nextStep) {
        OrderSagaInstance sagaInstance = orderSagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new NotFoundException("주문 SAGA 인스턴스를 찾을 수 없습니다"));
        sagaInstance.continueCompensation(nextStep);
        return SagaInstanceDto.from(sagaInstance);
    }

    @Transactional(readOnly = true)
    public List<SagaInstanceDto> getTimeouts(LocalDateTime timeout) {
        return orderSagaInstanceRepository.findByStartedAtBeforeAndSagaStatus(timeout, SagaStatus.STARTED)
                .stream().map(SagaInstanceDto::from).toList();
    }
}
