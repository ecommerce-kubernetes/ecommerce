package com.example.order_service.api.common.client.coupon;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class CouponErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == 404) {
            return new NotFoundException("쿠폰을 찾을 수 없습니다");
        }

        if (response.status() >= 500) {
            return new InternalServerException("쿠폰 서비스 장애 발생");
        }

        return new Exception("알 수 없는 에러");
    }
}
