package com.example.order_service.api.common.client.payment;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class TossPaymentErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == 403) {
            return new PaymentException("결제 승인이 실패했습니다", PaymentErrorCode.APPROVAL_FAIL);
        }

        if (response.status() == 404) {
            return new PaymentException("결제 가능 시간이 만료되었습니다", PaymentErrorCode.EXPIRED);
        }

        if (response.status() >= 500) {
            return new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
        }

        return new Exception("알 수 없는 에러");
    }
}
