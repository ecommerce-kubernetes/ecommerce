package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.application.port.dto.PaymentOrderStatus;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {

    public void validateOrderPending(PaymentOrderStatus status) {
        if (!status.equals(PaymentOrderStatus.PENDING)) {
            throw new BusinessException(PaymentErrorCode.ORDER_NOT_PENDING);
        }
    }
}
