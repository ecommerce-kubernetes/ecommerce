package com.example.order_service.api.common.client.product;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;

public class ProductErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == 404) {
            return new BusinessException(ExternalServiceErrorCode.PRODUCT_NOT_FOUND);
        }

        if (response.status() >= 500) {
            return new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
        }

        return new Exception("알 수 없는 에러");
    }
}
