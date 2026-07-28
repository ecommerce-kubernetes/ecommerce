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
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @RequestBody @Validated OrderCreateRequest request) {
        OrderCommand.Create command = request.toCommand(userPrincipal.getUserId());
        OrderResult.Create result = orderFacade.initialOrder(command);
        OrderCreateResponse response = OrderCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @PathVariable("orderId") Long orderId) {
        return null;
    }

    @GetMapping
    public ResponseEntity<PageDto<OrderSummaryResponse>> getOrders(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                   @ModelAttribute OrderSearchCondition condition,
                                                                   @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return null;
    }
}
