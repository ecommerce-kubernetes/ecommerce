package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderErrorCode;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import com.example.order_service.order.application.mapper.OrderCouponMapper;
import com.example.order_service.order.domain.service.dto.result.OrderCouponInfo;
import com.example.order_service.order.domain.service.dto.result.OrderProductAmount;
import com.example.order_service.order.infrastructure.client.coupon.OrderCouponAdaptor;
import com.example.order_service.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCouponGateway {
    private final OrderCouponAdaptor orderCouponAdaptor;
    private final CouponAdaptor couponAdaptor;
    private final OrderCouponMapper mapper;

    public OrderCouponResult.CouponValidation calculateCouponDiscount(Long userId, Long couponId, Long totalAmount) {
        CouponClientResponse.Calculate couponValidation = fetchCouponWithTranslation(userId, couponId, totalAmount);
        return mapper.toResult(couponValidation);
    }

    private CouponClientResponse.Calculate fetchCouponWithTranslation(Long userId, Long couponId, Long totalAmount) {
        try {
            return couponAdaptor.calculate(userId, couponId, totalAmount);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderErrorCode.ORDER_COUPON_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderErrorCode.ORDER_COUPON_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderErrorCode.ORDER_COUPON_UNAVAILABLE_SERVER_ERROR);
        }
    }

    public OrderCouponInfo calculateCouponDiscount(Long userId, Long couponId, OrderProductAmount productAmount){
        if (couponId == null) {
            return OrderCouponInfo.notUsed();
        }

        OrderCouponDiscountResponse coupon = orderCouponAdaptor.calculateDiscount(userId, couponId, productAmount.getSubTotalAmount());
        return mapToInfo(coupon);
    }

    private OrderCouponInfo mapToInfo(OrderCouponDiscountResponse coupon) {
        return OrderCouponInfo.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .discountAmount(coupon.getDiscountAmount())
                .build();
    }
}
