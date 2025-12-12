package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.BadRequestException;
import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderResponse;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.domain.model.Orders;
import com.example.order_service.api.order.domain.repository.OrdersRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import com.example.order_service.dto.OrderCalculationResult;
import com.example.order_service.dto.OrderValidationData;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.service.client.CouponClientService;
import com.example.order_service.service.client.UserClientService;
import com.example.order_service.service.client.dto.CouponResponse;
import com.example.order_service.service.client.dto.UserBalanceResponse;
import com.example.order_service.service.event.OrderEndMessageEvent;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderDomainService {
    private final ApplicationEventPublisher eventPublisher;
    private final OrdersRepository ordersRepository;
    private final CartProductClientService cartProductClientService;
    private final UserClientService userClientService;
    private final CouponClientService couponClientService;

    @Transactional
    public OrderCreationResult saveOrder(OrderCreationContext context){
        return null;
    }

}
