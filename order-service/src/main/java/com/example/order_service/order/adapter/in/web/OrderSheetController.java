package com.example.order_service.order.adapter.in.web;

import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.order.adapter.in.web.dto.ordersheet.request.*;
import com.example.order_service.order.adapter.in.web.dto.ordersheet.response.OrderSheetCreateResponse;
import com.example.order_service.order.adapter.in.web.dto.ordersheet.response.OrderSheetResponse;
import com.example.order_service.order.adapter.in.web.dto.ordersheet.response.OrderSheetUpdateResponse;
import com.example.order_service.order.application.service.ordersheet.OrderSheetService;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetUpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-sheets")
@PreAuthorize("hasRole('USER')")
public class OrderSheetController {
    private final OrderSheetService orderSheetService;

    @PostMapping("/direct")
    public ResponseEntity<OrderSheetCreateResponse> createDirectOrderSheet(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                           @RequestBody @Validated DirectOrderSheetCreateRequest request) {
        CreateDirectOrderSheetCommand command = request.toCommand(userPrincipal.getUserId());
        OrderSheetCreateResult result = orderSheetService.createDirectOrderSheet(command);
        OrderSheetCreateResponse response = OrderSheetCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/cart")
    public ResponseEntity<OrderSheetCreateResponse> createCartOrderSheet(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                         @RequestBody @Validated CartOrderSheetCreateRequest request) {
        CreateCartOrderSheetCommand command = request.toCommand(userPrincipal.getUserId());
        OrderSheetCreateResult result = orderSheetService.createCartOrderSheet(command);
        OrderSheetCreateResponse response = OrderSheetCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderSheetId}")
    public ResponseEntity<OrderSheetResponse> getOrderSheet(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                            @PathVariable("orderSheetId") Long orderSheetId) {
        OrderSheetResult result = orderSheetService.getOrderSheet(orderSheetId, userPrincipal.getUserId());
        OrderSheetResponse response = OrderSheetResponse.from(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{orderSheetId}/shipping-address")
    public ResponseEntity<OrderSheetUpdateResponse> updateShippingAddress(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                          @PathVariable("orderSheetId") Long orderSheetId,
                                                                          @RequestBody @Validated UpdateOrderSheetShippingAddressRequest request) {
        UpdateOrderSheetShippingAddressCommand command = request.toCommand(orderSheetId, userPrincipal.getUserId());
        OrderSheetUpdateResult result = orderSheetService.updateShippingAddress(command);
        OrderSheetUpdateResponse response = OrderSheetUpdateResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderSheetId}/item-coupons")
    public ResponseEntity<OrderSheetUpdateResponse> applyItemCoupon(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                              @PathVariable("orderSheetId") Long orderSheetId,
                                                              @RequestBody @Validated ApplyOrderSheetItemCouponsRequest request) {
        ApplyItemCouponsCommand command = request.toCommand(userPrincipal.getUserId(), orderSheetId);
        OrderSheetUpdateResult result = orderSheetService.applyItemCoupons(command);
        OrderSheetUpdateResponse response = OrderSheetUpdateResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderSheetId}/cart-coupon")
    public ResponseEntity<OrderSheetUpdateResponse> applyCartCoupon(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                              @PathVariable("orderSheetId") Long orderSheetId,
                                                              @RequestBody @Validated ApplyOrderSheetCartCouponRequest request) {
        ApplyCartCouponCommand command = request.toCommand(orderSheetId, userPrincipal.getUserId());
        OrderSheetUpdateResult result = orderSheetService.applyCartCoupon(command);
        OrderSheetUpdateResponse response = OrderSheetUpdateResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderSheetId}/points")
    public ResponseEntity<OrderSheetUpdateResponse> applyPoints(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @PathVariable("orderSheetId") Long orderSheetId,
                                                          @RequestBody @Validated ApplyOrderSheetPointRequest request) {
        ApplyPointCommand command = request.toCommand(orderSheetId, userPrincipal.getUserId());
        OrderSheetUpdateResult result = orderSheetService.applyPoints(command);
        OrderSheetUpdateResponse response = OrderSheetUpdateResponse.from(result);
        return ResponseEntity.ok(response);
    }
}
