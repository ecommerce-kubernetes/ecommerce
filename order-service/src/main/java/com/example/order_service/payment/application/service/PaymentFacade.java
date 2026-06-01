package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.mapper.PaymentMapper;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentFacade {
    private final OrderQueryService orderQueryService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentMapper mapper;
    private final PaymentGateway paymentGateway;

    public PaymentResult.PaymentApproval confirm(PaymentCommand.Confirm command) {
        OrderResult.Detail order = orderQueryService.getOrder(command.orderNo(), command.userId());
        if (order.status() != OrderStatus.PAYMENT_WAITING) {
            throw new BusinessException(PaymentErrorCode.ORDER_NOT_PAYMENT_WAITING);
        }
        if (!order.totalPaymentAmount().equals(command.amount())){
            throw new BusinessException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        PGPaymentCommand.Confirm gatewayCommand = PGPaymentCommand.Confirm.of(order.orderNo(), command.paymentKey(), order.totalPaymentAmount());
        PgPaymentResult.Approval confirmResult = paymentGateway.confirm(gatewayCommand);
        PaymentContext context = mapper.toContext(command.userId(), confirmResult);
        return paymentCommandService.save(context);
    }
}
