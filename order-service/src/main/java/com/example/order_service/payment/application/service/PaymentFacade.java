package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.payment.application.port.PaymentOrderPort;
import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGCancelResult;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.service.dto.command.PaymentCancelCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentConfirmResult;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.PaymentFailure;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CancelPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {
    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;
    private final PaymentOrderPort paymentOrderPort;
    private final PaymentPGPort paymentPGPort;
    private final PaymentValidator paymentValidator;
    private final PGErrorPolicy pgErrorPolicy;
    private final PaymentContextFactory contextFactory;

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

    public void cancel(PaymentCancelCommand command) {
        Optional<PaymentResult> paymentOptional = paymentQueryService.findCompletedPaymentByOrderId(command.orderId());

        if (paymentOptional.isEmpty()) {
            return;
        }
        PaymentResult payment = paymentOptional.get();
        paymentCommandService.refundPending(payment.paymentId(), payment.userId());

        PGCancelResult cancelResult = cancelPG(payment, command);
        CancelPaymentContext context = contextFactory.cancel(cancelResult.transactionKey(), cancelResult.amount(), cancelResult.canceledAt(), cancelResult.cancelReason());

        paymentCommandService.cancel(payment.paymentId(), payment.userId(), context);
    }

    private void approvePendingPayment(PaymentResult payment, PaymentConfirmCommand command) {
        ApprovePendingPaymentContext approvePendingContext = contextFactory.approvePending(command.amount(), command.provider(), command.paymentKey());
        paymentCommandService.approvePending(payment.paymentId(), payment.userId(), approvePendingContext);
    }

    private PGConfirmResult confirmPG(PaymentResult payment, PaymentConfirmCommand command) {
        try {
            return paymentPGPort.confirm(payment.orderId(), command.paymentKey(), payment.totalAmount(), command.provider());
        } catch (PortException e) {
            PaymentPGPortErrorCode errorCode = (PaymentPGPortErrorCode) e.getErrorCode();
            if (pgErrorPolicy.isAbortTargetOnApprove(errorCode)) {
                PaymentFailure failure = PaymentFailure.of(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
                paymentCommandService.abort(payment.paymentId(), payment.userId(), failure);
            }
            throw e;
        }
    }

    private PGCancelResult cancelPG(PaymentResult payment, PaymentCancelCommand command) {
        try {
            return paymentPGPort.cancel(payment.paymentKey(), command.cancelReason(), payment.provider());
        } catch (PortException e) {
            throw e;
        }
    }

    private void approvePayment(PGConfirmResult confirmResult, PaymentResult payment, PaymentConfirmCommand command) {
        try {
            ApprovePaymentContext approve = contextFactory.approve(confirmResult.method(), confirmResult.transactionKey(),
                    confirmResult.amount(), confirmResult.approvedAt());
            paymentCommandService.approve(payment.paymentId(), payment.userId(), approve);
        } catch (Exception e) {
            executeNetworkCancelAndAbort(command.paymentKey(), payment.paymentId(), payment.userId(), command.provider(),
                    "시스템 장애 또는 비즈니스 룰 위반으로 인한 자동 망취소");
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
