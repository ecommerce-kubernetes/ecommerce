package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.mapper.PaymentMapper;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    /**
     * 결제 생성
     * <p>
     * 결제 생성과 PG를 통한 결제 승인, 결제 레코드 생성 흐름을 담당
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
        PaymentResult.Default payment = paymentCommandService.save(context);
        PgPaymentResult.Approval pgResult = confirmWithPg(payment, order, command);
        return approveWithFallback(payment, pgResult);
    }

    private PgPaymentResult.Approval confirmWithPg(PaymentResult.Default payment, OrderResult.Detail order,
                                                   PaymentCommand.Confirm command) {
        try {
            PGPaymentCommand.Confirm gatewayCommand = PGPaymentCommand.Confirm.of(order.orderNo(), command.paymentKey(),
                    order.totalPaymentAmount());
            return paymentGateway.confirm(gatewayCommand);
        } catch (BusinessException e) {
            // [NOTE] 토스 결제 승인중 에러 발생시 결제를 취소 처리함
            // 토스 결제 승인중 타임아웃이 발생한다면 결제 승인이 처리되었는지 알 수 없음 따라서 대사 스케줄러를 통해 망취소를 진행함
            if (e.getErrorCode() != PaymentErrorCode.PAYMENT_TOSS_TIME_OUT_ERROR) {
                paymentFail(payment.id(), e.getMessage());
            } else {
                log.warn("토스 응답 타임아웃. 결제 미상 상태(READY) 유지 및 스케줄러 위임: {}", payment.id());
            }
            throw e;
        }
    }

    /**
     * [NOTE]
     * 내부 DB의 결제 상태를 ABORT로 변경
     * * [의도적인 예외 삼킴(Swallowing) 로직 포함]
     * 이미 PG사 통신에서 실패가 확정되어 금전적 피해가 없는 안전한 상태
     * 여기서 DB 업데이트 실패로 인해 예외를 밖으로 던지게 되면,
     * 원래의 비즈니스 에러가 DB 시스템 에러로 덮어씌워짐
     * 따라서 DB 갱신에 실패하더라도 예외를 삼키고 원래의 에러 흐름을 유지
     * READY 상태의 Payment는 대사 스케줄러가 정리
     */
    private void paymentFail(Long paymentId, String reason) {
        try {
            paymentCommandService.fail(paymentId, reason);
        } catch (Exception e) {
            log.error("결제 ABORT 변경 실패 {}", paymentId, e);
        }
    }

    private PaymentResult.PaymentApproval approveWithFallback(PaymentResult.Default payment,
                                                              PgPaymentResult.Approval pgResult) {
        try {
            PaymentContext.Approval approvalContext = mapper.toContext(payment.id(), pgResult);
            return paymentCommandService.done(approvalContext);
        } catch (Exception e) {
            boolean isCanceled = attemptNetworkCancel(payment.paymentKey());
            if (isCanceled) {
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
        } catch (Exception e) {
            log.error("망 취소 실패 {}", paymentKey, e);
            return false;
        }
    }

    /**
     * 결제 환불
     * <p>
     * 시스템 오류로 인한 결제 환불, 해당 주문의 결제 전체를 환불
     * </p>
     *
     * @param orderNo 주문 번호
     * @param reason  취소 이유
     */
    public void revert(String orderNo, String reason) {
        PaymentResult.Default payment = paymentQueryService.getPayment(orderNo);
        paymentCommandService.changeRefundPending(payment.orderNo());
        PGPaymentCommand.Cancel gatewayCommand = PGPaymentCommand.Cancel.ofFull(payment.paymentKey(), reason);
        PgPaymentResult.Cancellation cancel = paymentGateway.cancel(gatewayCommand);
        PaymentContext.Cancellation context = mapper.toContext(payment.id(), cancel);
        paymentCommandService.cancel(context);
    }
}
