package com.example.order_service.common.client.coupon;

import com.example.order_service.common.exception.CommonErrorCode;
import com.example.order_service.common.exception.ExternalServiceErrorCode;
import com.example.order_service.common.exception.business.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class CouponErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == 404) {
            return new BusinessException(ExternalServiceErrorCode.COUPON_NOT_FOUND);
        }

        if (response.status() == 409) {
            return new BusinessException(ExternalServiceErrorCode.COUPON_INVALID);
        }

        if (response.status() >= 500) {
            return new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
        }

        return new BusinessException(CommonErrorCode.UNKNOWN_ERROR);
    }
}
