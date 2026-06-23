package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.mapper.PaymentMapper;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciler {

    private static final int CHUNK_SIZE = 20;
    private static final long THROTTLE_MS = 100L;
    private static final int THRESHOLD_MINUTES = 3;

    private final PaymentQueryService queryService;
    private final PaymentCommandService commandService;
    private final PaymentGateway paymentGateway;
    private final PaymentMapper mapper;
    private final Clock clock;

    /**
     * 결제 승인 대사
     * <p>
     * 타임아웃된 READY 상태의 결제를 PG 사에 조회하여 결제된 상태라면 환불 후 실패 처리,
     * 결제 되지 않은 상태라면 바로 실패처리한다
     * </p>
     */
    public void reconcileReadyPayments() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(THRESHOLD_MINUTES);
        processReconciliation(
                () -> queryService.getReadyPaymentsBefore(threshold, CHUNK_SIZE),
                this::handleReadyPayment,
                "Payment Approval Reconciliation"
        );
    }

    /**
     * 환불 대사
     * <p>
     * 타임아웃된 REFUND_PENDING 상태의 결제를 PG 사에 조회하여 환불 된 상태라면 결제를 환불 처리,
     * 환불되지 않은 상태라면 환불 후 결제를 환불 처리함
     * </p>
     */
    public void reconcileRefundPendingPayments() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(THRESHOLD_MINUTES);
        processReconciliation(
                () -> queryService.getRefundPendingPaymentsBefore(threshold, CHUNK_SIZE),
                this::handleRefundPendingPayment,
                "Payment Refund Reconciliation"
        );
    }

    private void processReconciliation(
            Supplier<List<PaymentResult.Default>> paymentProvider,
            BiConsumer<PaymentResult.Default, PGPaymentResult.Inquiry> action,
            String taskName
    ) {
        List<PaymentResult.Default> payments = paymentProvider.get();
        if (payments.isEmpty()) {
            return;
        }

        for (PaymentResult.Default payment : payments) {
            try {
                PGPaymentResult.Inquiry inquire = paymentGateway.inquire(payment.paymentKey());
                action.accept(payment, inquire);
                Thread.sleep(THROTTLE_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[{}] 스로틀링 중 인터럽트 발생, 대사 조기 종료", taskName);
                break;
            } catch (Exception e) {
                log.error("[{}] 대사 처리 실패 paymentId = {}, orderNo = {}", taskName, payment.id(), payment.orderNo());
            }
        }
    }

    private void handleReadyPayment(PaymentResult.Default payment, PGPaymentResult.Inquiry inquire) {
        if (inquire.status() == PaymentStatus.ABORTED) {
            commandService.abort(payment.id(), inquire.failure().code());
        } else if (inquire.status() == PaymentStatus.CANCELED) {
            String cancelReason = (inquire.cancels() != null && !inquire.cancels().isEmpty())
                    ? inquire.lastCancel().cancelReason()
                    : "ALREADY_CANCELED_IN_PG";
            commandService.abort(payment.id(), cancelReason);
        } else if (inquire.status() == PaymentStatus.DONE) {
            PGPaymentCommand.Cancel cancelCommand = PGPaymentCommand.Cancel.ofFull(payment.paymentKey(), "PAYMENT_TIME_OUT");
            paymentGateway.cancel(cancelCommand);
            commandService.abort(payment.id(), "PAYMENT_TIME_OUT");
        }
    }

    private void handleRefundPendingPayment(PaymentResult.Default payment, PGPaymentResult.Inquiry inquire) {
        if (inquire.status() == PaymentStatus.CANCELED) {
            PaymentContext.Cancellation context = mapper.toContext(payment.id(), inquire.status(), inquire.lastCancel());
            commandService.cancel(context);
        } else if (inquire.status() == PaymentStatus.DONE) {
            PGPaymentCommand.Cancel gatewayCommand = PGPaymentCommand.Cancel.ofFull(payment.paymentKey(), "시스템 환불 대사 스케줄러에 의한 지연 취소");
            PGPaymentResult.Cancellation cancelResult = paymentGateway.cancel(gatewayCommand);
            PaymentContext.Cancellation context = mapper.toContext(payment.id(), PaymentStatus.CANCELED, cancelResult.lastCancel());
            commandService.cancel(context);
        }
    }
}
