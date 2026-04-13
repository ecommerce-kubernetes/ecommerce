package com.example.order_service.api.common.client.payment;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class TossPaymentErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public Exception decode(String s, Response response) {
        TossErrorResponse errorResponse = parseError(response);
        if (response.status() >= 500) {
            return new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
        }

        if (errorResponse == null || errorResponse.getCode() == null) {
            return new BusinessException(CommonErrorCode.UNKNOWN_ERROR);
        }

        TossPaymentErrorCode paymentError = TossPaymentErrorCode.findByTossErrorCode(errorResponse.getCode());
        return new BusinessException(paymentError.getBusinessErrorCode());
    }

    private TossErrorResponse parseError(Response response) {
        if (response.body() == null) {
            return null;
        }
        try (InputStream is = response.body().asInputStream()) {
            return objectMapper.readValue(is, TossErrorResponse.class);
        } catch (IOException e) {
            return null;
        }
    }
}
