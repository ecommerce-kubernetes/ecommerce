package com.example.order_service.payment.adapter.out.client;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.payment.application.port.PaymentOrderPort;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderStatus;
import com.example.order_service.payment.exception.PaymentOrderPortErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentOrderAdapter implements PaymentOrderPort {

    private final OrderQueryService orderQueryService;

    @Override
    public PaymentOrderResult getOrder(Long orderId, Long userId) {
        OrderResult orderResult = executeGetOrder(orderId, userId);
        return mapToPaymentOrderResult(orderResult);
    }

    private PaymentOrderResult mapToPaymentOrderResult(OrderResult orderResult) {
        PaymentOrderStatus status = mapToStatus(orderResult.status());

        return PaymentOrderResult.builder()
                .orderId(orderResult.orderId())
                .status(status)
                .orderName(orderResult.orderName())
                .totalAmount(orderResult.orderAmount().getTotalPaymentAmount())
                .build();
    }

    private OrderResult executeGetOrder(Long orderId, Long userId) {
        try {
            return orderQueryService.getOrder(orderId, userId);
        } catch (BusinessException e) {
            String errorCode = e.getErrorCode().name();

            PaymentOrderPortErrorCode code = switch (errorCode) {
                case "ORDER_NOT_FOUND" -> PaymentOrderPortErrorCode.ORDER_NOT_FOUND;
                default -> PaymentOrderPortErrorCode.ORDER_CLIENT_ERROR;
            };
            throw new PortException(code, e.getErrorCode().name(), e.getMessage());
        } catch (Exception e) {
            throw new PortException(PaymentOrderPortErrorCode.ORDER_SERVER_ERROR, "INTERNAL_SERVER_ERROR", e.getMessage());
        }
    }

    private PaymentOrderStatus mapToStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> PaymentOrderStatus.PENDING;
            case COMPLETED -> PaymentOrderStatus.COMPLETED;
            case PAID -> PaymentOrderStatus.PAID;
            case FAILED -> PaymentOrderStatus.FAILED;
            case PAYMENT_FAILED -> PaymentOrderStatus.PAYMENT_FAILED;
            case CANCELED -> PaymentOrderStatus.CANCELED;
        };
    }
}
