package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.exception.ErrorCode;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.mapper.PaymentMapper;
import com.example.order_service.payment.application.port.PaymentOrderPort;
import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentConfirmResult;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResultDeprecated;
import com.example.order_service.payment.domain.PaymentFailure;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import com.example.order_service.payment.exception.PaymentPortException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

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
    private final PaymentMapper mapper;
    private final PaymentGateway paymentGateway;
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

        PGConfirmResult confirmResult = confirmPG(payment);

        approvePayment(confirmResult, payment);

        return PaymentConfirmResult.of(payment.paymentId());
    }

    private void approvePayment(PGConfirmResult confirmResult, PaymentResult payment) {
        try {
            ApprovePaymentContext approve = contextFactory.approve(confirmResult.method(), confirmResult.transactionKey(), confirmResult.amount(), confirmResult.approvedAt());
            paymentCommandService.approve(payment.paymentId(), payment.userId(), approve);
        } catch (Exception e) {
            executeNetworkCancelAndAbort(payment.paymentKey(), payment.paymentId(), payment.userId(), payment.provider(), "시스템 장애 또는 비즈니스 룰 위반으로 인한 자동 망취소");
            throw e;
        }
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

    private PGConfirmResult confirmPG(PaymentResult payment) {
        try {
            return paymentPGPort.confirm(payment.orderId(), payment.paymentKey(), payment.totalAmount(), payment.provider());
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

    private void executeNetworkCancelAndAbort(String paymentKey, Long paymentId, Long userId, PaymentProvider provider, String reason) {
        try {
            paymentPGPort.netCancel(paymentKey, reason, provider);

            PaymentFailure failure = PaymentFailure.of("NETWORK_CANCEL", reason);
            paymentCommandService.abort(paymentId, userId, failure);

        } catch (PortException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_REFUND_PENDING);
        }
    }

    /**
     * 결제 환불
     * <p>
     * 시스템 오류로 인한 결제 환불, 해당 주문의 결제 전체를 환불
     * </p>
     *
     * @param paymentId 결제 번호
     * @param reason    취소 이유
     */
    public void revert(Long paymentId, String reason) {
        PaymentResultDeprecated.Default payment = null;
        LocalDateTime refundPendingAt = LocalDateTime.now(clock);
        paymentCommandService.changeRefundPending(payment.id(), refundPendingAt);
        try {
            PGPaymentCommand.Cancel gatewayCommand = PGPaymentCommand.Cancel.ofFull(payment.paymentKey(), reason);
            PGPaymentResult.Cancellation cancel = paymentGateway.cancel(gatewayCommand);
            PaymentContext.Cancellation context = mapper.toContext(payment.id(), cancel.status(), cancel.lastCancel());
            paymentCommandService.cancel(context);
        } catch (PaymentPortException e) {
            PaymentErrorCode errorCode = e.errorCode();
            switch (errorCode) {
                case PAYMENT_PG_SERVER_ERROR, PAYMENT_PG_UNAVAILABLE_ERROR, PAYMENT_PG_CIRCUIT_OPEN,
                     PAYMENT_PG_AUTH_ERROR ->
                        log.warn("[SAGA 환불 지연] 통신 장애로 인한 결제 취소 지연. 스케줄러 대기 paymentKey = {}", payment.paymentKey(), e);
                case PAYMENT_PG_ALREADY_CANCELED ->
                        log.info("[SAGA 환불 멱등성] 이미 결제가 취소됨. 확실한 취소 내역 동기화를 위해 환불 대사 스케줄러로 위임 paymentKey = {}", payment.paymentKey());
                default -> log.error("[SAGA 환불 거절] PG사 거절. 수동 환불 처리 필요. paymentKey = {}", payment.paymentKey(), e);
            }
        } catch (Exception e) {
            log.error("[SAGA 환불 시스템 에러] 로직 오류 또는 DB 반영 실패. 스케줄러 재처리 예정: {}", payment.paymentKey(), e);
        }
    }
}
