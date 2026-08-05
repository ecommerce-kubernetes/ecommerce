package com.example.order_service.payment.api.web;

import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.payment.api.web.dto.request.PaymentConfirmRequest;
import com.example.order_service.payment.api.web.dto.request.PaymentCreateRequest;
import com.example.order_service.payment.api.web.dto.response.PaymentApprovalResponse;
import com.example.order_service.payment.api.web.dto.response.PaymentCreateResponse;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResultDeprecated;
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
@PreAuthorize("hasRole('USER')")
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping
    public ResponseEntity<PaymentCreateResponse> createPayment(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                               @RequestBody @Validated PaymentCreateRequest request) {
        PaymentCreateCommand command = request.toCommand(userPrincipal.getUserId());
        PaymentCreateResult result = paymentFacade.create(command);
        PaymentCreateResponse response = PaymentCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentApprovalResponse> paymentConfirm(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                                  @RequestBody @Validated PaymentConfirmRequest request) {
        PaymentCommand.Confirm command = request.toCommand(userPrincipal.getUserId());
        PaymentResultDeprecated.PaymentApproval confirm = paymentFacade.confirm(command);
        PaymentApprovalResponse response = PaymentApprovalResponse.from(confirm);
        return ResponseEntity.ok(response);
    }
}
