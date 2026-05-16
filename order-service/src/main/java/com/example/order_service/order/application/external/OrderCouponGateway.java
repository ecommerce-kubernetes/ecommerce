package com.example.order_service.order.application.external;

import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import com.example.order_service.order.application.mapper.OrderCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCouponGateway {
    private final CouponAdaptor couponAdaptor;
    private final OrderCouponMapper mapper;

    public OrderCouponResult.CouponValidation calculateCouponDiscount(Long userId, Long couponId, Long totalAmount) {
        CouponClientResponse.Calculate couponValidation = fetchCouponWithTranslation(userId, couponId, totalAmount);
        return null;
    }

    private CouponClientResponse.Calculate fetchCouponWithTranslation(Long userId, Long couponId, Long totalAmount) {
//        try {
//            return couponAdaptor.calculate(userId, couponId, totalAmount);
//        } catch (ExternalClientException e) {
//            throw new BusinessException(OrderErrorCode.ORDER_COUPON_CLIENT_ERROR);
//        } catch (ExternalServerException e) {
//            throw new BusinessException(OrderErrorCode.ORDER_COUPON_SERVER_ERROR);
//        } catch (ExternalSystemUnavailableException e) {
//            throw new BusinessException(OrderErrorCode.ORDER_COUPON_UNAVAILABLE_SERVER_ERROR);
//        }
        return null;
    }

}
