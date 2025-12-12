package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.order.domain.repository.OrdersRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderDomainService {
    private final ApplicationEventPublisher eventPublisher;
    private final OrdersRepository ordersRepository;
    private final CartProductClientService cartProductClientService;

    @Transactional
    public OrderCreationResult saveOrder(OrderCreationContext context){
        return null;
    }

}
