package com.example.order_service.api.common.client.payment;

import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.exception.server.InternalServerException;
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

        return new InternalServerException("토스 페이먼츠 서비스 에러");
    }
}
