package com.example.order_service.order.api.web;

import com.example.order_service.common.dto.PageDto;
import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.order.api.web.dto.order.request.OrderCreateRequest;
import com.example.order_service.order.api.web.dto.order.request.OrderSearchCondition;
import com.example.order_service.order.api.web.dto.order.response.OrderCreateResponse;
import com.example.order_service.order.api.web.dto.order.response.OrderResponse;
import com.example.order_service.order.api.web.dto.order.response.OrderSummaryResponse;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    private final OrderFacade orderFacade;
    private final OrderQueryService orderQueryService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @RequestBody @Validated OrderCreateRequest request) {
        CreateOrderCommand command = request.toCommand(userPrincipal.getUserId());
        OrderCreateResult result = orderFacade.createOrder(command);
        OrderCreateResponse response = OrderCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @PathVariable("orderId") Long orderId) {
        OrderResult result = orderQueryService.getOrder(orderId, userPrincipal.getUserId());
        OrderResponse response = OrderResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<OrderSummaryResponse>> getOrders(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                   @ModelAttribute OrderSearchCondition condition,
                                                                   @PageableDefault(size = 20, page = 0, sort = {}) Pageable pageable) {
        OrderSearchCommand command = OrderSearchCommand.of(condition.getSort(), condition.getYear(), condition.getProductName(), pageable);
        Page<OrderSummaryResult> result = orderQueryService.getOrders(userPrincipal.getUserId(), command);
        PageDto<OrderSummaryResponse> response = PageDto.of(result, OrderSummaryResponse::from);
        return ResponseEntity.ok(response);
    }
}
