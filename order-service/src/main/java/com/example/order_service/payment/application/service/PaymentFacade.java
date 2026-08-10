package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.payment.application.port.PaymentOrderPort;
import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentConfirmResult;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.PaymentFailure;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {
    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;
    private final PaymentOrderPort paymentOrderPort;
    private final PaymentPGPort paymentPGPort;
    private final PaymentValidator paymentValidator;
    private final PaymentContextFactory contextFactory;
    private final Clock clock;

    public PaymentCreateResult create(PaymentCreateCommand command) {
        PaymentOrderResult order = paymentOrderPort.getOrder(command.orderId(), command.userId());

        paymentValidator.validateOrderPending(order.status());

        CreatePaymentContext context = contextFactory.create(order.orderId(), command.userId(), order.totalAmount());
        Long paymentId = paymentCommandService.create(context);

        PaymentResult payment = paymentQueryService.getPayment(paymentId, command.userId());
        return PaymentCreateResult.from(payment, order);
    }

    public PaymentConfirmResult approve(PaymentConfirmCommand command) {
        PaymentResult payment = paymentQueryService.getPayment(command.paymentId(), command.userId());

        approvePendingPayment(payment, command);

        PGConfirmResult confirmResult = confirmPG(payment, command);

        approvePayment(confirmResult, payment, command);

        return PaymentConfirmResult.of(payment.paymentId());
    }

    private void approvePendingPayment(PaymentResult payment, PaymentConfirmCommand command) {
        ApprovePendingPaymentContext approvePendingContext = contextFactory.approvePending(command.amount(), command.provider(), command.paymentKey());
        try {
            paymentCommandService.approvePending(payment.paymentId(), payment.userId(), approvePendingContext);
        } catch (BusinessException e) {
            if (e.getErrorCode().equals(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH)) {
                PaymentFailure failure = PaymentFailure.of(e.getErrorCode().name(), e.getErrorCode().getMessage());
                paymentCommandService.abort(command.paymentId(), command.userId(), failure);
            }
            throw e;
        }
    }

    private PGConfirmResult confirmPG(PaymentResult payment, PaymentConfirmCommand command) {
        try {
            return paymentPGPort.confirm(payment.orderId(), command.paymentKey(), payment.totalAmount(), command.provider());
        } catch (PortException e) {
            if (e.getErrorCode().equals(PaymentPGPortErrorCode.PG_INSUFFICIENT_BALANCE) ||
                    e.getErrorCode().equals(PaymentPGPortErrorCode.PG_METHOD_REJECTED) ||
                    e.getErrorCode().equals(PaymentPGPortErrorCode.PG_POLICY_RESTRICTED) ||
                    e.getErrorCode().equals(PaymentPGPortErrorCode.PG_INVALID_REQUEST) ||
                    e.getErrorCode().equals(PaymentPGPortErrorCode.PG_NOT_FOUND) ||
                    e.getErrorCode().equals(PaymentPGPortErrorCode.PG_AUTH_ERROR) ||
                    e.getErrorCode().equals(PaymentPGPortErrorCode.UNSUPPORTED_PROVIDER) ||
                    e.getErrorCode().equals(PaymentPGPortErrorCode.PG_CIRCUIT_OPEN)) {
                PaymentFailure failure = PaymentFailure.of(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
                paymentCommandService.abort(payment.paymentId(), payment.userId(), failure);
            }
            throw e;
        }
    }

    private void approvePayment(PGConfirmResult confirmResult, PaymentResult payment, PaymentConfirmCommand command) {
        try {
            ApprovePaymentContext approve = contextFactory.approve(confirmResult.method(), confirmResult.transactionKey(), confirmResult.amount(), confirmResult.approvedAt());
            paymentCommandService.approve(payment.paymentId(), payment.userId(), approve);
        } catch (Exception e) {
            executeNetworkCancelAndAbort(command.paymentKey(), payment.paymentId(), payment.userId(), command.provider(), "시스템 장애 또는 비즈니스 룰 위반으로 인한 자동 망취소");
            throw e;
        }
    }

    private void executeNetworkCancelAndAbort(String paymentKey, Long paymentId, Long userId, PaymentProvider provider, String reason) {
        try {
            paymentPGPort.netCancel(paymentKey, reason, provider);

            PaymentFailure failure = PaymentFailure.of("NETWORK_CANCEL", reason);
            paymentCommandService.abort(paymentId, userId, failure);

        } catch (PortException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_REFUND_PENDING);
        }
    }
}
