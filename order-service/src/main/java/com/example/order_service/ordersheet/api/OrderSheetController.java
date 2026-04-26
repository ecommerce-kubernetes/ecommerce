package com.example.order_service.ordersheet.api;

import com.example.order_service.api.common.security.model.UserPrincipal;
import com.example.order_service.ordersheet.api.dto.request.OrderSheetRequest;
import com.example.order_service.ordersheet.api.dto.response.OrderSheetResponse;
import com.example.order_service.ordersheet.application.OrderSheetService;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        OrderSheetResult.Default result = orderSheetService.createOrderSheet(command);
        OrderSheetResponse.Create response = OrderSheetResponse.Create.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
