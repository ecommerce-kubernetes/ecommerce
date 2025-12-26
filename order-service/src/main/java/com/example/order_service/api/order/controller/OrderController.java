package com.example.order_service.api.order.controller;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.common.util.DomainType;
import com.example.order_service.api.common.util.validator.PageableValidator;
import com.example.order_service.api.common.util.validator.PageableValidatorFactory;
import com.example.order_service.api.order.application.OrderApplicationService;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderResponse;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.controller.dto.request.OrderConfirmRequest;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final OrderApplicationService orderApplicationService;
    private final PageableValidatorFactory factory;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody @Validated CreateOrderRequest createOrderRequest,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal){

        CreateOrderDto createOrderDto = CreateOrderDto.of(userPrincipal, createOrderRequest);
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderDto);
        return ResponseEntity.status(HttpStatus.SC_ACCEPTED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<OrderResponse>> getOrders(@RequestHeader("X-User-Id") Long userId,
                                                                       @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                                                                       @RequestParam(value = "year",required = false) String year,
                                                                       @RequestParam(value = "keyword", required = false) String keyword){
        PageableValidator validator = factory.getValidator(DomainType.ORDER);
        Pageable validatedPageable = validator.validate(pageable);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<OrderResponse> confirm(@RequestBody @Validated OrderConfirmRequest request) {
        OrderResponse orderResponse = orderApplicationService.confirmOrder(request.getOrderId(), request.getPaymentKey());
        return ResponseEntity.ok(orderResponse);
    }

}
