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
@Service
@RequiredArgsConstructor
public class PaymentFacade {
    private final OrderQueryService orderQueryService;
    private final PaymentCommandService paymentCommandService;
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
        PGPaymentCommand.Confirm gatewayCommand = PGPaymentCommand.Confirm.of(order.orderNo(), command.paymentKey(), order.totalPaymentAmount());
        PgPaymentResult.Approval confirm = paymentGateway.confirm(gatewayCommand);
        PaymentContext.Approval approval = mapper.toContext(payment.id(), confirm);
        return paymentCommandService.approve(approval);
    }
}
