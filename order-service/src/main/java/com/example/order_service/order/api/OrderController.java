package com.example.order_service.order.api;

import com.example.order_service.common.dto.PageDto;
import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.order.api.dto.request.OrderConfirmRequest;
import com.example.order_service.order.api.dto.request.OrderRequest;
import com.example.order_service.order.api.dto.request.OrderSearchCondition;
import com.example.order_service.order.api.dto.response.OrderResponse;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import com.example.order_service.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
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
    public ResponseEntity<OrderResponse.Create> createOrder(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                            @RequestBody @Validated OrderRequest.Create request) {
        OrderCommand.Create command = request.toCommand(userPrincipal.getUserId());
        OrderResult.Create result = orderFacade.initialOrder(command);
        OrderResponse.Create response = OrderResponse.Create.from(result);
        return ResponseEntity.status(HttpStatus.SC_ACCEPTED).body(response);
    }

    @GetMapping("/{orderNo}")
    public ResponseEntity<OrderResponse.Detail> getOrder(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @PathVariable("orderNo") String orderNo) {
        OrderResult.Detail result = orderQueryService.getOrder(orderNo, userPrincipal.getUserId());
        OrderResponse.Detail response = OrderResponse.Detail.from(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<OrderResponse.Summary>> getOrders(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                @ModelAttribute OrderSearchCondition condition,
                                                                @PageableDefault(size = 20, page = 0) Pageable pageable) {
        OrderSearchCommand command = condition.toCommand();
        Page<OrderResult.Summary> orders = orderQueryService.getOrders(userPrincipal.getUserId(), command, pageable);
        PageDto<OrderResponse.Summary> summaryPageDto = PageDto.of(orders, OrderResponse.Summary::from);
        return ResponseEntity.ok(summaryPageDto);
    }

    @PostMapping("/confirm")
    public ResponseEntity<OrderDetailResponse> confirm(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @RequestBody @Validated OrderConfirmRequest request) {
        OrderDetailResponse response = orderFacade.confirmOrderPayment(request.getOrderNo(),
                userPrincipal.getUserId(), request.getPaymentKey(), request.getAmount());
        return ResponseEntity.ok(response);
    }
}
