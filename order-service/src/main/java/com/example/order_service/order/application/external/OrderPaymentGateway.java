package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.TossAdaptor;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.order.application.dto.result.OrderPaymentResult;
import com.example.order_service.order.application.mapper.OrderPaymentMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPaymentGateway {
    private final TossAdaptor tossAdaptor;
    private final OrderPaymentMapper mapper;

    public OrderPaymentResult.Payment confirmOrderPayment(String orderNo, String paymentKey, Long amount) {
        TossClientResponse.Confirm confirm = confirmPaymentWithTranslation(orderNo, paymentKey, amount);
        return mapper.toPaymentResult(confirm);
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
