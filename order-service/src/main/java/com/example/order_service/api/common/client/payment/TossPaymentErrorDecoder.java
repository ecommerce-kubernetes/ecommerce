package com.example.order_service.api.common.client.payment;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
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
        TossErrorResponse error = parseError(response);
        String errorCode = (error != null) ? error.getCode() : "UNKNOWN";
        String message = (error != null) ? error.getMessage() : "알 수 없는 에러";
        int status = response.status();

        if (status == 400) {
            return handle400Error(errorCode, message);
        }
        if (status == 401) {
            return handle401Error(errorCode, message);
        }
        if (status == 403) {
            return handle403Error(errorCode, message);
        }
        if (status == 404) {
            return handle404Error(errorCode, message);
        }

        if (status >= 500) {
            log.error("토스 페이먼츠 서버 에러 status = {}, code = {}, message = {}", status, errorCode, message);
            return new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
        }

        return new BusinessException(CommonErrorCode.UNKNOWN_ERROR);
    }

    private Exception handle400Error(String code, String message) {
        return switch (code) {
            case "ALREADY_PROCESSED_PAYMENT" -> new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_PROCEED_PAYMENT);
            case "INVALID_REQUEST" -> new BusinessException(PaymentErrorCode.PAYMENT_BAD_REQUEST);
            case "INVALID_API_KEY" -> {
                log.error("결제 : 잘못된 시크릿 키 연동, message = {}", message);
                yield new BusinessException(PaymentErrorCode.PAYMENT_SYSTEM_ERROR);
            }
            default -> new BusinessException(PaymentErrorCode.PAYMENT_BAD_REQUEST);
        };
    }

    private Exception handle401Error(String code, String message) {
        log.error("결제 : 인증되지 않은 시크릿 키, message = {}", message);
        return new BusinessException(PaymentErrorCode.PAYMENT_SYSTEM_ERROR);
    }

    private Exception handle403Error(String code, String message) {
        return switch (code) {
            case "REJECT_ACCOUNT_PAYMENT" -> new BusinessException(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE);
            case "FORBIDDEN_REQUEST" -> {
                log.error("결제 : 허용되지 않은 요청, message={}", message);
                yield new BusinessException(PaymentErrorCode.PAYMENT_SYSTEM_ERROR);
            }
            default -> {
                log.error("Unknown 403 Error {}", code);
                yield new BusinessException(PaymentErrorCode.PAYMENT_SYSTEM_ERROR);
            }
        };
    }

    private Exception handle404Error(String code, String message) {
        return switch (code) {
            case "NOT_FOUND_PAYMENT" -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND);
            case "NOT_FOUND_PAYMENT_SESSION" -> new BusinessException(PaymentErrorCode.PAYMENT_TIMEOUT);
            default -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        };
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
