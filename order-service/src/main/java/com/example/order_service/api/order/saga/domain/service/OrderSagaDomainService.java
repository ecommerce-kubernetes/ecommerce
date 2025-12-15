package com.example.order_service.api.order.saga.domain.service;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import org.springframework.stereotype.Service;

@Service
public class OrderSagaDomainService {

    public SagaInstanceDto saveOrderSagaInstance(Long orderId, Payload payload){
        return null;
    }
}
