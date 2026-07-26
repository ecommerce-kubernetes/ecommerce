package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
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
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.exception.PaymentPortException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

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
        } catch (PaymentPortException e) {
            PaymentErrorCode errorCode = e.errorCode();
            switch (errorCode) {
                case PAYMENT_PG_SERVER_ERROR,
                     PAYMENT_PG_UNAVAILABLE_ERROR,
                     PAYMENT_PG_CIRCUIT_OPEN,
                     PAYMENT_PG_AUTH_ERROR:
                    log.warn("[결제 승인 지연] PG 응답 불명. 대사 처리 대기. paymentId = {}", payment.id(), e);
                    throw e;
                case PAYMENT_PG_INSUFFICIENT_BALANCE,
                     PAYMENT_PG_METHOD_REJECTED,
                     PAYMENT_PG_POLICY_RESTRICTED,
                     PAYMENT_PG_INVALID_REQUEST,
                     PAYMENT_PG_ALREADY_PROCESSED,
                     PAYMENT_PG_NOT_FOUND,
                     PAYMENT_PG_ALREADY_CANCELED:
                    log.info("[결제 승인 거절] PG사 거절. paymentId = {}, 사유 = {}", payment.id(), errorCode);
                    abortPayment(payment.id(), errorCode.getCode());
                    throw e;
                default:
                    throw new IllegalStateException(
                            "Unhandled PaymentErrorCode : " + errorCode
                    );
            }
        }
    }

    private PaymentResult.PaymentApproval approveWithFallback(PaymentResult.Default payment,
                                                              PGPaymentResult.Approval pgResult) {
        try {
            PaymentContext.Approval approvalContext = mapper.toContext(payment.id(), pgResult);
            return paymentCommandService.approve(approvalContext);
        } catch (Exception e) {
            boolean isCanceled = attemptNetworkCancel(payment.paymentKey());
            if (isCanceled) {
                abortPayment(payment.id(), "NET-CANCEL");
                throw new BusinessException(PaymentErrorCode.PAYMENT_AUTO_CANCELED);
            } else {
                throw new BusinessException(PaymentErrorCode.PAYMENT_REFUND_PENDING);
            }
        }
    }

    private boolean attemptNetworkCancel(String paymentKey) {
        try {
            PGPaymentCommand.Cancel cancelCommand = PGPaymentCommand.Cancel.ofFull(paymentKey, "내부 DB 저장 실패로 인한 망취소");
            paymentGateway.cancel(cancelCommand);
            return true;
        } catch (PaymentPortException e) {
            PaymentErrorCode errorCode = e.errorCode();
            switch (errorCode) {
                case PAYMENT_PG_SERVER_ERROR, PAYMENT_PG_UNAVAILABLE_ERROR, PAYMENT_PG_CIRCUIT_OPEN, PAYMENT_PG_AUTH_ERROR:
                    log.warn("[망취소] PG 응답 불명. 대사 처리 대기. paymentKey = {}", paymentKey);
                    return false;
                case PAYMENT_PG_ALREADY_CANCELED:
                    return true;
                default:
                    log.info("[망취소] PG사 거절. paymentKey = {}", paymentKey);
                    return false;
            }
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
            log.error("결제 ABORT 변경 실패 paymentId = {}, reason = {}", paymentId, reason, e);
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
                case PAYMENT_PG_SERVER_ERROR, PAYMENT_PG_UNAVAILABLE_ERROR, PAYMENT_PG_CIRCUIT_OPEN, PAYMENT_PG_AUTH_ERROR ->
                        log.warn("[SAGA 환불 지연] 통신 장애로 인한 결제 취소 지연. 스케줄러 대기 paymentKey = {}", payment.paymentKey(), e);
                case PAYMENT_PG_ALREADY_CANCELED -> log.info("[SAGA 환불 멱등성] 이미 결제가 취소됨. 확실한 취소 내역 동기화를 위해 환불 대사 스케줄러로 위임 paymentKey = {}", payment.paymentKey());
                default -> log.error("[SAGA 환불 거절] PG사 거절. 수동 환불 처리 필요. paymentKey = {}", payment.paymentKey(), e);
            }
        } catch (Exception e) {
            log.error("[SAGA 환불 시스템 에러] 로직 오류 또는 DB 반영 실패. 스케줄러 재처리 예정: {}", payment.paymentKey(), e);
        }
    }
}
