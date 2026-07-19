package com.example.order_service.order.api.web;

import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.order.api.web.dto.request.*;
import com.example.order_service.order.api.web.dto.response.OrderSheetCreateResponse;
import com.example.order_service.order.api.web.dto.response.OrderSheetResponse;
import com.example.order_service.order.api.web.dto.response.OrderSheetResponseDeprecate;
import com.example.order_service.order.application.service.ordersheet.OrderSheetService;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResultDeprecate;
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
                                                            @PathVariable("orderSheetId") String orderSheetId) {
        OrderSheetResult result = orderSheetService.getOrderSheet(orderSheetId, userPrincipal.getUserId());
        OrderSheetResponse response = OrderSheetResponse.from(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{orderSheetId}/shipping-address")
    public ResponseEntity<OrderSheetResponse> updateShippingAddress(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                    @PathVariable("orderSheetId") String orderSheetId,
                                                                    @RequestBody @Validated UpdateOrderSheetShippingAddressRequest request) {
        UpdateOrderSheetShippingAddressCommand command = request.toCommand(orderSheetId, userPrincipal.getUserId());
        OrderSheetResult result = orderSheetService.updateShippingAddress(command);
        OrderSheetResponse response = OrderSheetResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderSheetId}/items/{orderSheetItemId}/coupon")
    public ResponseEntity<OrderSheetResponse> applyItemCoupon(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                              @PathVariable("orderSheetId") String orderSheetId,
                                                              @PathVariable("orderSheetItemId") String orderSheetItemId,
                                                              @RequestBody @Validated ApplyOrderSheetItemCouponRequest request) {
        ApplyItemCouponCommand command = request.toCommand(userPrincipal.getUserId(), orderSheetId, orderSheetItemId);
        OrderSheetResult result = orderSheetService.applyItemCoupon(command);
        OrderSheetResponse response = OrderSheetResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderSheetId}/cart-coupon")
    public ResponseEntity<OrderSheetResponse> applyCartCoupon(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                              @PathVariable("orderSheetId") String orderSheetId,
                                                              @RequestBody @Validated ApplyOrderSheetCartCouponRequest request) {
        ApplyCartCouponCommand command = request.toCommand(orderSheetId, userPrincipal.getUserId());
        OrderSheetResult result = orderSheetService.applyCartCoupon(command);
        OrderSheetResponse response = OrderSheetResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{sheetId}/points")
    public ResponseEntity<OrderSheetResponseDeprecate.Detail> updateUsedPoints(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                               @PathVariable("sheetId") String sheetId,
                                                                               @RequestBody @Validated OrderSheetRequest.UpdateUsedPoints request) {
        OrderSheetCommand.UpdatePoints command = request.toCommand(sheetId, userPrincipal.getUserId());
        OrderSheetResultDeprecate.Detail result = orderSheetService.updatePoints(command);
        OrderSheetResponseDeprecate.Detail response = OrderSheetResponseDeprecate.Detail.from(result);
        return ResponseEntity.ok(response);
    }
}
