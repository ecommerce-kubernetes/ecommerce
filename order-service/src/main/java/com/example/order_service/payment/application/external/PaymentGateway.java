package com.example.order_service.payment.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.TossAdaptor;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgMapper;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentGateway {
    private final TossAdaptor tossAdaptor;
    private final PgMapper pgMapper;

    public PGPaymentResult.Approval confirm(PGPaymentCommand.Confirm command) {
        TossClientResponse.Confirm confirm = fetchTossConfirmWithTransactional(command);
        return pgMapper.toResult(confirm);
    }

    public PGPaymentResult.Cancellation cancel(PGPaymentCommand.Cancel command) {
        TossClientResponse.Cancel cancel = fetchTossCancelWithTransactional(command);
        return pgMapper.toResult(cancel);
    }

    public PGPaymentResult.Inquiry inquire(String paymentKey) {
        return null;
    }

    private TossClientResponse.Confirm fetchTossConfirmWithTransactional(PGPaymentCommand.Confirm command) {
        try {
            return tossAdaptor.confirmPayment(command.orderNo(), command.paymentKey(), command.amount().longValue());
        } catch (ExternalClientException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_TOSS_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_TOSS_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_TOSS_UNAVAILABLE_ERROR);
        }
    }

    private TossClientResponse.Cancel fetchTossCancelWithTransactional(PGPaymentCommand.Cancel command) {
        try {
            Long cancelAmount = command.amount() == null ? null : command.amount().longValue();
            return tossAdaptor.cancelPayment(command.paymentKey(), command.cancelReason(), cancelAmount);
        } catch (ExternalClientException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_TOSS_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_TOSS_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_TOSS_UNAVAILABLE_ERROR);
        }
    }
}
