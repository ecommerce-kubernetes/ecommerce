package com.example.order_service.payment.api.web;

import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.payment.api.web.dto.request.PaymentRequest;
import com.example.order_service.payment.api.web.dto.response.PaymentResponse;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse.PaymentApproval> paymentConfirm(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                          @RequestBody @Validated PaymentRequest.Confirm request) {
        PaymentCommand.Confirm command = request.toCommand(userPrincipal.getUserId());
        PaymentResult.PaymentApproval confirm = paymentFacade.confirm(command);
        PaymentResponse.PaymentApproval response = PaymentResponse.PaymentApproval.from(confirm);
        return ResponseEntity.ok(response);
    }
}
