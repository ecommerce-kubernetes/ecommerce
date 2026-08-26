package com.example.order_service.payment.application.port;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.dto.PGCancelResult;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PGInquiryResult;
import com.example.order_service.payment.domain.PaymentProvider;

public interface PaymentPGPort {

    PGConfirmResult confirm(Long orderId, String paymentKey, Money amount, PaymentProvider provider);

    void netCancel(String paymentKey, String cancelReason, PaymentProvider provider);

    PGCancelResult cancel(String paymentKey, String cancelReason, PaymentProvider provider);

    PGInquiryResult inquiry(String paymentKey, PaymentProvider provider);
}
