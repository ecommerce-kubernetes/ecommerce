package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public PaymentResult.PaymentApproval confirm(PaymentCommand.Confirm command) {
        return null;
    }
}
