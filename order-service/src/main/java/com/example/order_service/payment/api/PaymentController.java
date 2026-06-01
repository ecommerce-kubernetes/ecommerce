package com.example.order_service.payment.api;

import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.payment.api.dto.request.PaymentRequest;
import com.example.order_service.payment.api.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class PaymentController {

    @PostMapping
    public ResponseEntity<PaymentResponse.PaymentApproval> paymentConfirm(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                          @RequestBody PaymentRequest.Confirm request) {
        return null;
    }
}
