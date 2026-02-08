package com.example.order_service.api.common.client.user;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == 404) {
            return new BusinessException(ExternalServiceErrorCode.USER_NOT_FOUND);
        }

        if (response.status() >= 500) {
            return new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
        }

        return new BusinessException(CommonErrorCode.UNKNOWN_ERROR);
    }
}
