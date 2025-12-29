package com.example.order_service.api.order.controller;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.OrderApplicationService;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.application.dto.result.OrderListResponse;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.controller.dto.request.OrderConfirmRequest;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody @Validated CreateOrderRequest createOrderRequest,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal){

        CreateOrderDto createOrderDto = CreateOrderDto.of(userPrincipal, createOrderRequest);
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderDto);
        return ResponseEntity.status(HttpStatus.SC_ACCEPTED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("orderId") Long orderId) {
        OrderDetailResponse response = orderApplicationService.getOrder(userPrincipal, orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<OrderListResponse>> getOrders(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                @ModelAttribute OrderSearchCondition condition) {
        PageDto<OrderListResponse> orders = orderApplicationService.getOrders(userPrincipal, condition);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/confirm")
    public ResponseEntity<OrderDetailResponse> confirm(@RequestBody @Validated OrderConfirmRequest request) {
        OrderDetailResponse orderDetailResponse = orderApplicationService.confirmOrder(request.getOrderId(), request.getPaymentKey());
        return ResponseEntity.ok(orderDetailResponse);
    }

}
