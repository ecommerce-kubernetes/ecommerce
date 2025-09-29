package com.example.order_service.service.client;

import com.example.order_service.client.CouponClient;
import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.service.client.dto.CouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponClientService {
    private final CouponClient couponClient;
    private final MessageSourceUtil ms;

    public CouponResponse fetchCouponByUserCouponId(Long userId, Long userCouponId){
        return couponClient.getCoupon(userId, userCouponId);
    }
}
