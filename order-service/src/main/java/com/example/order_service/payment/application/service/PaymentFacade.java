package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.mapper.PaymentMapper;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제를 담당하는 오케스트레이션 서비스
 * <p>
 * 외부 결제 PG 호출 및 결제 생성 오케스트레이션을 담당
 * </p>
 *
 * @author 최민식
 * @since 2026 06. 02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {
    private final OrderQueryService orderQueryService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;
    private final PaymentMapper mapper;
    private final PaymentGateway paymentGateway;
    private final Clock clock;

    /**
     * 결제 승인 처리
     * <p>
     * 주문 검증 후 PG 승인 요청을 수행하고
     * 내부 결제 상태를 승인 처리한다.
     * 승인 처리 실패 시 망취소를 시도한다.
     * </p>
     *
     * @param command 결제 생성 커맨드
     * @return 결제 승인 결과
     */
    public PaymentResult.PaymentApproval confirm(PaymentCommand.Confirm command) {
        OrderResult.Detail order = orderQueryService.getOrder(command.orderNo(), command.userId());
        if (order.status() != OrderStatus.PENDING) {
            throw new BusinessException(PaymentErrorCode.ORDER_NOT_PENDING);
        }
        if (!order.totalPaymentAmount().equals(command.amount())) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        PaymentContext.Create context = mapper.toContext(command);
        PaymentResult.Default payment = paymentCommandService.create(context);
        PGPaymentResult.Approval pgResult = confirmWithPg(payment, order, command);
        return approveWithFallback(payment, pgResult);
    }

    private PGPaymentResult.Approval confirmWithPg(PaymentResult.Default payment, OrderResult.Detail order,
                                                   PaymentCommand.Confirm command) {
        try {
            PGPaymentCommand.Confirm gatewayCommand = PGPaymentCommand.Confirm.of(order.orderNo(), command.paymentKey(),
                    order.totalPaymentAmount());
            return paymentGateway.confirm(gatewayCommand);
        } catch (BusinessException e) {
            abortPayment(payment.id(), e.getMessage());
            throw e;
        }
    }

    private PaymentResult.PaymentApproval approveWithFallback(PaymentResult.Default payment,
                                                              PGPaymentResult.Approval pgResult) {
        try {
            PaymentContext.Approval approvalContext = mapper.toContext(payment.id(), pgResult);
            return paymentCommandService.approve(approvalContext);
        } catch (Exception e) {
            boolean isCanceled = attemptNetworkCancel(payment.paymentKey(), "내부 DB 저장 실패로 인한 망취소");
            if (isCanceled) {
                abortPayment(payment.id(), "NET-CANCEL");
                throw new BusinessException(PaymentErrorCode.PAYMENT_AUTO_CANCELED);
            } else {
                throw new BusinessException(PaymentErrorCode.PAYMENT_REFUND_PENDING);
            }
        }
    }

    private boolean attemptNetworkCancel(String paymentKey, String cancelReason) {
        try {
            PGPaymentCommand.Cancel cancelCommand = PGPaymentCommand.Cancel.ofFull(paymentKey, cancelReason);
            paymentGateway.cancel(cancelCommand);
            return true;
        } catch (Exception e) {
            log.error("망 취소 실패 {}", paymentKey, e);
            return false;
        }
    }

    /**
     * [NOTE]
     * 내부 DB의 결제 상태를 ABORT로 변경
     * [의도적인 예외 삼킴(Swallowing) 로직 포함]
     * 이미 PG사 통신에서 실패가 확정되어 금전적 피해가 없는 안전한 상태
     * 여기서 DB 업데이트 실패로 인해 예외를 밖으로 던지게 되면,
     * 원래의 비즈니스 에러가 DB 시스템 에러로 덮어씌워짐
     * 따라서 DB 갱신에 실패하더라도 예외를 삼키고 원래의 에러 흐름을 유지
     * READY 상태의 Payment는 대사 스케줄러가 정리
     */
    private void abortPayment(Long paymentId, String reason) {
        try {
            paymentCommandService.abort(paymentId, reason);
        } catch (Exception e) {
            log.error("결제 ABORT 변경 실패 {}", paymentId, e);
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
        PaymentResult.Default payment = paymentQueryService.getPayment(paymentId);
        paymentCommandService.changeRefundPending(payment.id());
        try {
            PGPaymentCommand.Cancel gatewayCommand = PGPaymentCommand.Cancel.ofFull(payment.paymentKey(), reason);
            PGPaymentResult.Cancellation cancel = paymentGateway.cancel(gatewayCommand);
            PaymentContext.Cancellation context = mapper.toContext(payment.id(), cancel.status(), cancel.lastCancel());
            paymentCommandService.cancel(context);
        } catch (Exception e) {
            log.error("[SAGA 보상 지연] PG사 취소 또는 최종 DB 반영 실패. 스케줄러가 재처리 예정: {}", payment.paymentKey(), e);
        }
    }

    /**
     * 결제 승인 대사
     * <p>
     * 타임아웃된 READY 상태의 결제를 PG 사에 조회하여 결제된 상태라면 환불 후 실패 처리,
     * 결제 되지 않은 상태라면 바로 실패처리한다
     * </p>
     */
    public void reconcileReadyPayments() {
        int chunkSize = 20;
        LocalDateTime thresholdTime = LocalDateTime.now(clock).minusMinutes(3);
        List<PaymentResult.Default> payments = paymentQueryService.getReadyPaymentBefore(thresholdTime, chunkSize);

        if (payments.isEmpty()) {
            return;
        }

        for (PaymentResult.Default payment : payments) {
            try {
                PGPaymentResult.Inquiry inquire = paymentGateway.inquire(payment.paymentKey());
                if (inquire.status() == PaymentStatus.ABORTED) {
                    paymentCommandService.abort(payment.id(), inquire.failure().code());
                } else if (inquire.status() == PaymentStatus.CANCELED) {
                    String cancelReason = "ALREADY_CANCELED_IN_PG";
                    if (inquire.cancels() != null && !inquire.cancels().isEmpty()) {
                        cancelReason = inquire.lastCancel().cancelReason();
                    }
                    paymentCommandService.abort(payment.id(), cancelReason);
                } else if (inquire.status() == PaymentStatus.DONE) {
                    boolean isCanceled = attemptNetworkCancel(payment.paymentKey(), "PAYMENT_TIME_OUT");
                    if (isCanceled) {
                        paymentCommandService.abort(payment.id(), "PAYMENT_TIME_OUT");
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[Payment Approval Reconciliation Error] 스로틀링 중 인터럽트 발생. 대사를 조기 종료");
                break;
            } catch (Exception e) {
                log.error("[Payment Approval Reconciliation Error] paymentId = {}, orderNo = {}", payment.id(), payment.orderNo());
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
        int chunkSize = 20;
        LocalDateTime thresholdTime = LocalDateTime.now(clock).minusMinutes(3);
        List<PaymentResult.Default> payments = paymentQueryService.getRefundPendingPaymentBefore(thresholdTime, chunkSize);

        if (payments.isEmpty()) {
            return;
        }

        for (PaymentResult.Default payment : payments) {
            try {
                PGPaymentResult.Inquiry inquire = paymentGateway.inquire(payment.paymentKey());
                if (inquire.status() == PaymentStatus.CANCELED) {
                    PGPaymentResult.CancelReceipt cancelReceipt = inquire.lastCancel();
                    PaymentContext.Cancellation context = mapper.toContext(payment.id(), PaymentStatus.CANCELED, cancelReceipt);
                    paymentCommandService.cancel(context);
                } else if (inquire.status() == PaymentStatus.DONE) {
                    boolean isCanceled = attemptNetworkCancel(payment.paymentKey(), "PAYMENT_TIME_OUT");
                    if (isCanceled) {
                        PGPaymentResult.Inquiry result = paymentGateway.inquire(payment.paymentKey());
                        PGPaymentResult.CancelReceipt cancelReceipt = result.lastCancel();
                        PaymentContext.Cancellation context = mapper.toContext(payment.id(), PaymentStatus.CANCELED, cancelReceipt);
                        paymentCommandService.cancel(context);
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[Payment Refund Reconciliation Error] 스로틀링 중 인터럽트 발생. 대사를 조기 종료");
                break;
            } catch (Exception e) {
                log.error("[Payment Refund Reconciliation Error] paymentId = {}, orderNo = {}", payment.id(), payment.orderNo());
            }
        }
    }
}
