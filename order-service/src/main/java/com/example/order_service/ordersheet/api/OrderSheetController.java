package com.example.order_service.ordersheet.api;

import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.ordersheet.api.dto.request.OrderSheetRequest;
import com.example.order_service.ordersheet.api.dto.response.OrderSheetResponse;
import com.example.order_service.ordersheet.application.OrderSheetAppService;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
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
    private final OrderSheetAppService orderSheetAppService;

    @PostMapping
    public ResponseEntity<OrderSheetResponse.Create> createOrderSheet(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                      @RequestBody @Validated OrderSheetRequest.Create request) {
        OrderSheetCommand.Create command = request.toCommand(userPrincipal.getUserId());
        OrderSheetResult.Create result = orderSheetAppService.createOrderSheet(command);
        OrderSheetResponse.Create response = OrderSheetResponse.Create.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sheetId}")
    public ResponseEntity<OrderSheetResponse.Detail> getOrderSheet(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                          @PathVariable("sheetId") String sheetId) {
        OrderSheetResult.Detail orderSheet = orderSheetAppService.getOrderSheet(sheetId, userPrincipal.getUserId());
        OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(orderSheet);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
