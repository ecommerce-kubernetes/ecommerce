package com.example.order_service.order.infrastructure.adaptor.client;

import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.port.CouponPortErrorCode;
import com.example.order_service.common.exception.port.DefaultPortException;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.infrastructure.gateway.CouponGateway;
import com.example.order_service.order.application.port.OrderCouponPort;
import com.example.order_service.order.application.port.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.port.dto.result.CartCouponResult;
import com.example.order_service.order.application.port.dto.result.ItemCouponResult;
import com.example.order_service.order.application.port.dto.result.OrderCouponResult;
import com.example.order_service.order.infrastructure.adaptor.mapper.OrderCouponPortMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class OrderCouponAdaptor implements OrderCouponPort {
    private final CouponGateway couponGateway;
    private final OrderCouponPortMapper orderCouponPortMapper;

    @Override
    public ItemCouponResult getItemCoupon(Long userId, Long itemCouponId) {
        ItemCouponResponse response = executeGetItemCoupon(userId, itemCouponId);
        return orderCouponPortMapper.mapToItemCouponResult(response);
    }

    private ItemCouponResponse executeGetItemCoupon(Long userId, Long itemCouponId) {
        return executeWithExceptionTranslation(() -> couponGateway.getItemCoupon(userId, itemCouponId));
    }

    @Override
    public CartCouponResult getCartCoupon(Long userId, Long cartCouponId) {
        CartCouponResponse response = executeGetCartCoupon(userId, cartCouponId);
        return orderCouponPortMapper.mapToCartcouponResult(response);
    }

    private CartCouponResponse executeGetCartCoupon(Long userId, Long cartCouponId) {
        return executeWithExceptionTranslation(() -> couponGateway.getCartCoupon(userId, cartCouponId));
    }

    private <T> T executeWithExceptionTranslation(Supplier<T> apiCall) {
        try {
            return apiCall.get();
        } catch (ExternalClientException e) {
            throw new DefaultPortException(CouponPortErrorCode.COUPON_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultPortException(CouponPortErrorCode.COUPON_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultPortException(CouponPortErrorCode.COUPON_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultPortException(CouponPortErrorCode.COUPON_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        }
    }

    @Deprecated
    public OrderCouponResult.Calculate calculate(OrderCouponCommand.Calculate command) {
        return null;
    }
}
