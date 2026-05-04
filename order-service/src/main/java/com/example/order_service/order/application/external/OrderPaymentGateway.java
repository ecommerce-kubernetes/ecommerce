package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderErrorCode;
import com.example.order_service.common.exception.business.code.PaymentErrorCode;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.TossAdaptor;
import com.example.order_service.infrastructure.client.TossFeignClient;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.order.application.dto.result.OrderPaymentResult;
import com.example.order_service.order.application.mapper.OrderPaymentMapper;
import com.example.order_service.order.domain.model.PaymentMethod;
import com.example.order_service.order.domain.model.PaymentStatus;
import com.example.order_service.order.domain.service.dto.result.OrderPaymentInfo;
import com.example.order_service.order.infrastructure.client.payment.TossPaymentAdaptor;
import com.example.order_service.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class OrderPaymentGateway {
    private final TossPaymentAdaptor tossPaymentAdaptor;
    private final TossAdaptor tossAdaptor;
    private final OrderPaymentMapper mapper;

    public OrderPaymentInfo confirmOrderPaymentdeprecated(String orderNo, String paymentKey, Long amount) {
        TossPaymentConfirmResponse paymentResponse = tossPaymentAdaptor.confirmPayment(orderNo, paymentKey, amount);
        validatePaymentStatus(paymentResponse);
        return mapToOrderPaymentInfo(paymentResponse);
    }

    public OrderPaymentResult.Payment confirmOrderPayment(String orderNo, String paymentKey, Long amount) {
        TossClientResponse.Confirm confirm = confirmPaymentWithTranslation(orderNo, paymentKey, amount);
        return mapper.toPaymentResult(confirm);
    }

    public void cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        tossPaymentAdaptor.cancelPayment(paymentKey, cancelReason, cancelAmount);
    }

    private OrderPaymentInfo mapToOrderPaymentInfo(TossPaymentConfirmResponse response) {
        return OrderPaymentInfo.builder()
                .orderNo(response.getOrderId())
                .paymentKey(response.getPaymentKey())
                .totalAmount(response.getTotalAmount())
                .status(PaymentStatus.from(response.getStatus()))
                .method(PaymentMethod.from(response.getMethod()))
                .approvedAt(parseDateTime(response.getApprovedAt()))
                .build();
    }

    private void validatePaymentStatus(TossPaymentConfirmResponse response) {
        if (PaymentStatus.from(response.getStatus()) != PaymentStatus.DONE &&
                PaymentStatus.from(response.getStatus()) != PaymentStatus.WAITING_FOR_DEPOSIT) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_APPROVAL_FAIL);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(dateTimeString).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }

    private TossClientResponse.Confirm confirmPaymentWithTranslation(String orderNo, String paymentKey, Long amount) {
        try {
            return tossAdaptor.confirmPayment(orderNo, paymentKey, amount);
        } catch (ExternalClientException e){
            throw new BusinessException(OrderErrorCode.ORDER_PAYMENT_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderErrorCode.ORDER_PAYMENT_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderErrorCode.ORDER_PAYMENT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
