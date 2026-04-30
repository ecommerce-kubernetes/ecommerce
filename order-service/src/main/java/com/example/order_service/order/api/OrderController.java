package com.example.order_service.order.api;

import com.example.order_service.common.dto.PageDto;
import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.order.api.dto.request.CreateOrderRequest;
import com.example.order_service.order.api.dto.request.OrderConfirmRequest;
import com.example.order_service.order.api.dto.request.OrderSearchCondition;
import com.example.order_service.order.application.OrderAppService;
import com.example.order_service.order.application.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.dto.command.CreateOrderItemCommand;
import com.example.order_service.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.order.application.dto.result.OrderListResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderAppService orderAppService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @RequestBody @Validated CreateOrderRequest request){
        List<CreateOrderItemCommand> orderItems = mappingCreateOrderItemDto(request);
        CreateOrderCommand command = CreateOrderCommand.builder()
                .userId(userPrincipal.getUserId())
                .orderItemCommands(orderItems)
                .deliveryAddress(request.getDeliveryAddress())
                .couponId(request.getCouponId())
                .pointToUse(request.getPointToUse())
                .expectedPrice(request.getExpectedPrice())
                .build();

        CreateOrderResponse response = orderAppService.initialOrder(command);
        return ResponseEntity.status(HttpStatus.SC_ACCEPTED).body(response);
    }

    @GetMapping("/{orderNo}")
    public ResponseEntity<OrderDetailResponse> getOrder(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                        @PathVariable("orderNo") String orderNo) {
        OrderDetailResponse response = orderAppService.getOrder(userPrincipal.getUserId(), orderNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<OrderListResponse>> getOrders(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                @ModelAttribute OrderSearchCondition condition) {
        PageDto<OrderListResponse> orders = orderAppService.getOrders(userPrincipal.getUserId(), condition);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/confirm")
    public ResponseEntity<OrderDetailResponse> confirm(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @RequestBody @Validated OrderConfirmRequest request) {
        OrderDetailResponse response = orderAppService.confirmOrderPayment(request.getOrderNo(),
                userPrincipal.getUserId(), request.getPaymentKey(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    private List<CreateOrderItemCommand> mappingCreateOrderItemDto(CreateOrderRequest request){
        return request.getItems().stream().map(item -> CreateOrderItemCommand.of(item.getProductVariantId(), item.getQuantity()))
                .toList();
    }
}
