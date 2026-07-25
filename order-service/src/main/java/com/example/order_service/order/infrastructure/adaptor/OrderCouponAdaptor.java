package com.example.order_service.order.infrastructure.adaptor;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.CouponGatewayErrorCode;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.infrastructure.gateway.CouponGateway;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.order.application.port.OrderCouponPort;
import com.example.order_service.order.application.port.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.port.dto.result.CartCouponResult;
import com.example.order_service.order.application.port.dto.result.ItemCouponResult;
import com.example.order_service.order.application.port.dto.result.OrderCouponResult;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import com.example.order_service.order.domain.vo.CartCouponSnapshot;
import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class OrderCouponAdaptor implements OrderCouponPort {
    private final CouponGateway couponGateway;

    @Override
    public ItemCouponResult getItemCoupon(Long userId, Long itemCouponId) {
        ItemCouponResponse response = executeGetItemCoupon(userId, itemCouponId);
        return mapItemCouponResult(response);
    }

    private ItemCouponResponse executeGetItemCoupon(Long userId, Long itemCouponId) {
        return executeWithExceptionTranslation(() -> couponGateway.getItemCoupon(userId, itemCouponId));
    }

    private ItemCouponResult mapItemCouponResult(ItemCouponResponse response) {
        CouponDiscountPolicy discountPolicy = switch (response.discountType()) {
            case "FIXED" -> new FixedCouponDiscountPolicy(Money.wons(response.discountAmount()));
            case "RATE" -> new RateCouponDiscountPolicy(response.discountRate(), Money.wons(response.maxDiscountAmount()));
            default -> throw new DefaultGatewayException(CouponGatewayErrorCode.COUPON_CLIENT_ERROR, "UNSUPPORTED_TYPE", "처리할 수 없는 쿠폰 타입입니다.");
        };

        ItemCouponSnapshot itemCouponSnapshot = ItemCouponSnapshot.of(
                response.itemCouponId(),
                response.name(),
                discountPolicy,
                response.applyQuantityLimit()
        );

        return ItemCouponResult.builder()
                .itemCoupon(itemCouponSnapshot)
                .build();
    }

    @Override
    public CartCouponResult getCartCoupon(Long userId, Long cartCouponId) {
        CartCouponResponse response = executeGetCartCoupon(userId, cartCouponId);
        return mapToCartCouponResult(response);
    }

    private CartCouponResponse executeGetCartCoupon(Long userId, Long cartCouponId) {
        return executeWithExceptionTranslation(() -> couponGateway.getCartCoupon(userId, cartCouponId));
    }

    private CartCouponResult mapToCartCouponResult(CartCouponResponse response) {
        CouponDiscountPolicy discountPolicy = switch (response.discountType()) {
            case "FIXED" -> new FixedCouponDiscountPolicy(Money.wons(response.discountAmount()));
            case "RATE" -> new RateCouponDiscountPolicy(response.discountRate(), Money.wons(response.maxDiscountAmount()));
            default -> throw new DefaultGatewayException(CouponGatewayErrorCode.COUPON_CLIENT_ERROR, "UNSUPPORTED_TYPE", "처리할 수 없는 쿠폰 타입입니다.");
        };

        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(response.cartCouponId(),
                response.name(),
                discountPolicy,
                Money.wons(response.minimumPaymentAmount()));

        return CartCouponResult.builder()
                .cartCoupon(cartCoupon)
                .build();
    }

    private <T> T executeWithExceptionTranslation(Supplier<T> apiCall) {
        try {
            return apiCall.get();
        } catch (ExternalClientException e) {
            throw new DefaultGatewayException(CouponGatewayErrorCode.COUPON_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultGatewayException(CouponGatewayErrorCode.COUPON_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultGatewayException(CouponGatewayErrorCode.COUPON_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultGatewayException(CouponGatewayErrorCode.COUPON_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        }
    }

    @Deprecated
    public OrderCouponResult.Calculate calculate(OrderCouponCommand.Calculate command) {
        return null;
    }
}
