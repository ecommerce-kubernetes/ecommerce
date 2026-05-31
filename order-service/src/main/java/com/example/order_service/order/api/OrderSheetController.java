package com.example.order_service.order.api;

import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.order.api.dto.request.OrderSheetRequest;
import com.example.order_service.order.api.dto.response.OrderSheetResponse;
import com.example.order_service.order.application.service.ordersheet.OrderSheetService;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
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

    @PostMapping
    public ResponseEntity<OrderSheetResponse.Create> createOrderSheet(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                      @RequestBody @Validated OrderSheetRequest.Create request) {
        OrderSheetCommand.Create command = request.toCommand(userPrincipal.getUserId());
        OrderSheetResult.Create result = orderSheetService.createOrderSheet(command);
        OrderSheetResponse.Create response = OrderSheetResponse.Create.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sheetId}")
    public ResponseEntity<OrderSheetResponse.Detail> getOrderSheet(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                          @PathVariable("sheetId") String sheetId) {
        OrderSheetResult.Detail orderSheet = orderSheetService.getOrderSheet(sheetId, userPrincipal.getUserId());
        OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(orderSheet);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{sheetId}/shipping-address")
    public ResponseEntity<OrderSheetResponse.Detail> updateShippingAddress(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                           @PathVariable("sheetId") String sheetId,
                                                                           @RequestBody @Validated OrderSheetRequest.UpdateShippingAddress request) {
        OrderSheetCommand.UpdateShippingAddress command = request.toCommand(sheetId, userPrincipal.getUserId());
        OrderSheetResult.Detail result = orderSheetService.updateShippingAddress(command);
        OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{sheetId}/points")
    public ResponseEntity<OrderSheetResponse.Detail> updateUsedPoints(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                      @PathVariable("sheetId") String sheetId,
                                                                      @RequestBody @Validated OrderSheetRequest.UpdateUsedPoints request) {
        OrderSheetCommand.UpdatePoints command = request.toCommand(sheetId, userPrincipal.getUserId());
        OrderSheetResult.Detail result = orderSheetService.updatePoints(command);
        OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{sheetId}/sheet-items/{sheetItemId}/coupon")
    public ResponseEntity<OrderSheetResponse.Detail> updateItemCoupon(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                      @PathVariable("sheetId") String sheetId,
                                                                      @PathVariable("sheetItemId") String sheetItemId,
                                                                      @RequestBody @Validated OrderSheetRequest.UpdateCoupon request) {
        OrderSheetCommand.UpdateItemCoupon command = request.toCommand(sheetId, sheetItemId, userPrincipal.getUserId());
        OrderSheetResult.Detail result = orderSheetService.updateItemCoupon(command);
        OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{sheetId}/cart-coupon")
    public ResponseEntity<OrderSheetResponse.Detail> updateCartCoupon(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                      @PathVariable("sheetId") String sheetId,
                                                                      @RequestBody @Validated OrderSheetRequest.UpdateCoupon request) {
        OrderSheetCommand.UpdateCartCoupon command = request.toCommand(sheetId, userPrincipal.getUserId());
        OrderSheetResult.Detail result = orderSheetService.updateCartCoupon(command);
        OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
        return ResponseEntity.ok(response);
    }
}
