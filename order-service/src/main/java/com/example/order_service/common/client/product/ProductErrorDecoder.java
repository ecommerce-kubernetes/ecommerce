package com.example.order_service.common.client.product;

import com.example.order_service.common.exception.CommonErrorCode;
import com.example.order_service.common.exception.ExternalServiceErrorCode;
import com.example.order_service.common.exception.business.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == 404) {
            return new BusinessException(ExternalServiceErrorCode.PRODUCT_NOT_FOUND);
        }

        if (response.status() >= 500) {
            return new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
        }

        return new BusinessException(CommonErrorCode.UNKNOWN_ERROR);
    }
}
