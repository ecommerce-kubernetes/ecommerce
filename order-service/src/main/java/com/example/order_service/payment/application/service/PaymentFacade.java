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
            if (e.getErrorCode() != PaymentErrorCode.PAYMENT_TOSS_SERVER_ERROR
                    && e.getErrorCode() != PaymentErrorCode.PAYMENT_TOSS_UNAVAILABLE_ERROR) {
                paymentFail(payment.id(), e.getErrorCode().getCode());
            }
            throw e;
        }
    }

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
            attemptNetworkCancel(payment.paymentKey());
            throw new BusinessException(PaymentErrorCode.PAYMENT_SYSTEM_ERROR);
        }
    }

    private void attemptNetworkCancel(String paymentKey) {
        try {
            PGPaymentCommand.Cancel cancelCommand = PGPaymentCommand.Cancel.ofFull(paymentKey, "내부 DB 저장 실패로 인한 망취소");
            paymentGateway.cancel(cancelCommand);
        } catch (Exception e) {
            log.error("망 취소 실패 {}", paymentKey, e);
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
