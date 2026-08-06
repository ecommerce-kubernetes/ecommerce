package com.example.order_service.order.infrastructure.adaptor.client;

import com.example.order_service.common.exception.PortException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import com.example.order_service.infrastructure.gateway.CouponGateway;
import com.example.order_service.order.application.port.OrderCouponPort;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.exception.OrderCouponPortErrorCode;
import com.example.order_service.order.infrastructure.adaptor.mapper.OrderCouponPortMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class OrderCouponAdaptor implements OrderCouponPort {
    private final CouponGateway couponGateway;
    private final OrderCouponPortMapper orderCouponPortMapper;

    @Override
    public CartCouponResult getCartCoupon(Long userId, Long cartCouponId) {
        CartCouponResponse response = executeGetCartCoupon(userId, cartCouponId);
        return orderCouponPortMapper.mapToCartCouponResult(response);
    }

    private CartCouponResponse executeGetCartCoupon(Long userId, Long cartCouponId) {
        return executeWithExceptionTranslation(() -> couponGateway.getCartCoupon(userId, cartCouponId));
    }

    @Override
    public ItemCouponsResult getItemCoupons(Long userId, List<Long> itemCouponIds) {
        ItemCouponsResponse response = executeGetItemCoupons(userId, itemCouponIds);
        return orderCouponPortMapper.mapToItemCouponsResult(response);
    }

    private ItemCouponsResponse executeGetItemCoupons(Long userId, List<Long> itemCouponIds){
        return executeWithExceptionTranslation(() -> couponGateway.getItemCoupons(userId, itemCouponIds));
    }

    private <T> T executeWithExceptionTranslation(Supplier<T> apiCall) {
        try {
            return apiCall.get();
        } catch (ExternalClientException e) {
            throw new PortException(OrderCouponPortErrorCode.COUPON_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new PortException(OrderCouponPortErrorCode.COUPON_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new PortException(OrderCouponPortErrorCode.COUPON_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new PortException(OrderCouponPortErrorCode.COUPON_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        }
    }
}
