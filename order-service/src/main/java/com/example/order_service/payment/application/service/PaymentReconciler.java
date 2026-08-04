package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.mapper.PaymentMapper;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.exception.PaymentPortException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
        List<PaymentResult.Default> payments = queryService.getReadyPaymentsBefore(threshold, CHUNK_SIZE);
        processPayments(payments, "결제 승인 대사",
                this::processSingleReadyPayment,
                this::handleReadyPaymentError
        );
    }

    private void processSingleReadyPayment(PaymentResult.Default payment) {
        PGPaymentResult.Inquiry inquire = paymentGateway.inquire(payment.paymentKey());
        switch (inquire.status()) {
            case ABORTED -> commandService.abort(payment.id(), inquire.failure().code());
            case CANCELED -> commandService.abort(payment.id(), inquire.lastCancel().cancelReason());
            case DONE -> cancelDonePayment(payment, "결제 승인 대사", "결제 대기 시간 초과",
                    cancellation -> commandService.abort(payment.id(), cancellation.lastCancel().cancelReason()),
                    () -> commandService.changeApprovalManualCheck(payment.id()));
            default -> {
                log.error("[결제 승인 대사] 예상치 못한 PG 상태 반환. 수동 확인 필요 paymentKey = {}, status = {}", payment.paymentKey(), inquire.status());
                commandService.changeApprovalManualCheck(payment.id());
            }
        }
    }

    private void handleReadyPaymentError(PaymentResult.Default payment, PaymentPortException e) {
        PaymentErrorCode errorCode = e.errorCode();
        switch (errorCode) {
            case PAYMENT_PG_NOT_FOUND -> {
                log.info("[결제 승인 대사] PG 결제 내역 없음 실패 처리 paymentKey = {}", payment.paymentKey());
                commandService.abort(payment.id(), "NOT_FOUND_IN_PG");
            }
            case PAYMENT_PG_SERVER_ERROR, PAYMENT_PG_UNAVAILABLE_ERROR, PAYMENT_PG_CIRCUIT_OPEN ->
                    log.warn("[결제 승인 대사] PG 결제 조회 장애, 다음 스케줄러 대기 paymentKey = {}", payment.paymentKey(), e);
            default -> {
                log.error("[결제 승인 대사] 단건 조회 확정 실패. 수동 확인 필요. paymentKey = {}", payment.paymentKey(), e);
                commandService.changeApprovalManualCheck(payment.id());
            }
        }
    }

    private void processPayments(List<PaymentResult.Default> payments, String taskName, Consumer<PaymentResult.Default> task,
                                 BiConsumer<PaymentResult.Default, PaymentPortException> errorHandler) {
        if (payments.isEmpty()) {
            return;
        }

        for (PaymentResult.Default payment : payments) {
            try {
                task.accept(payment);
                Thread.sleep(THROTTLE_MS);
            } catch (InterruptedException e) {
                log.info("[{}] 조기 종료", taskName);
                Thread.currentThread().interrupt();
                return;
            } catch (PaymentPortException e) {
                errorHandler.accept(payment, e);
            } catch (Exception e) {
                log.error("[{}] 내부 시스템 에러. 다음 스케줄러 대기. paymentKey = {}", taskName, payment.paymentKey(), e);
            }
        }
    }

    private void cancelDonePayment(PaymentResult.Default payment, String taskName, String reason, Consumer<PGPaymentResult.Cancellation> onSuccess,
                                   Runnable onManualCheck) {
        try {
            PGPaymentCommand.Cancel cancelCommand = PGPaymentCommand.Cancel.ofFull(payment.paymentKey(), reason);
            PGPaymentResult.Cancellation cancellation = paymentGateway.cancel(cancelCommand);
            onSuccess.accept(cancellation);
        } catch (PaymentPortException e) {
            PaymentErrorCode errorCode = e.errorCode();
            switch (errorCode) {
                case PAYMENT_PG_SERVER_ERROR, PAYMENT_PG_UNAVAILABLE_ERROR, PAYMENT_PG_CIRCUIT_OPEN ->
                        log.warn("[{}] DONE 건 망취소 통신 장애. READY 상태 유지 (다음 배치 재시도) paymentKey = {}", taskName, payment.paymentKey());
                case PAYMENT_PG_ALREADY_CANCELED ->
                        log.info("[{}] 망취소 중 이미 취소됨 확인. 상세 내역 동기화를 위해 다음 배치 대기 paymentKey = {}", taskName, payment.paymentKey());
                default -> {
                    log.error("[{}] 망취소 확정 거절. 수동 확인 필요 paymentKey = {}", taskName, payment.paymentKey(), e);
                    onManualCheck.run();
                }
            }
        }
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
        List<PaymentResult.Default> payments = queryService.getRefundPendingPaymentsBefore(threshold, CHUNK_SIZE);
        processPayments(payments, "결제 환불 대사",
                this::reconcileRefundPendingPayment,
                this::reconcileRefundPendingErrorHandle
        );
    }

    private void reconcileRefundPendingPayment(PaymentResult.Default payment) {
        PGPaymentResult.Inquiry inquire = paymentGateway.inquire(payment.paymentKey());
        switch (inquire.status()) {
            case CANCELED -> {
                PaymentContext.Cancellation context = mapper.toContext(payment.id(), inquire.status(), inquire.lastCancel());
                commandService.cancel(context);
            }
            case DONE -> cancelDonePayment(payment, "결제 환불 대사",
                    "환불 대기 시간 초과",
                    cancellation -> {
                        PaymentContext.Cancellation context = mapper.toContext(payment.id(), PaymentStatus.CANCELED, cancellation.lastCancel());
                        commandService.cancel(context);
                    },
                    () -> commandService.changeRefundManualCheck(payment.id())
            );
            default -> {
                log.error("[결제 환불 대사] 예상치 못한 PG 상태 반환. 수동 확인 필요 paymentKey = {}, status = {}", payment.paymentKey(), inquire.status());
                commandService.changeRefundManualCheck(payment.id());
            }
        }
    }

    private void reconcileRefundPendingErrorHandle(PaymentResult.Default payment, PaymentPortException e) {
        PaymentErrorCode errorCode = e.errorCode();
        switch (errorCode) {
            case PAYMENT_PG_NOT_FOUND -> {
                log.error("[결제 환불 대사] PG 결제 내역 없음. 데이터 정합성 오류, 수동 확인 필요 paymentKey = {}", payment.paymentKey());
                commandService.changeRefundManualCheck(payment.id());
            }
            case PAYMENT_PG_SERVER_ERROR, PAYMENT_PG_UNAVAILABLE_ERROR, PAYMENT_PG_CIRCUIT_OPEN ->
                    log.warn("[결제 환불 대사] PG 결제 조회 장애, 다음 스케줄러 대기 paymentKey = {}", payment.paymentKey(), e);
            default -> {
                log.error("[결제 환불 대사] 단건 조회 확정 실패. 수동 확인 필요. paymentKey = {}", payment.paymentKey(), e);
                commandService.changeRefundManualCheck(payment.id());
            }
        }
    }
}
