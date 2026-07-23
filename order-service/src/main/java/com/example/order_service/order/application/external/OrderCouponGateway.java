package com.example.order_service.order.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.result.CartCouponResult;
import com.example.order_service.order.application.external.dto.result.ItemCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import com.example.order_service.order.domain.vo.CartCouponSnapshot;
import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 주문 쿠폰 도메인 통신을 담당하는 Gateway 서비스
 * <p>
 * 쿠폰 도메인의 응답을 서비스 레이어의 Result 로 매핑하여 반환
 * 쿠폰 도메인 통신중 발생하는 예외를 비지니스 예외로 변환
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 22
 */
@Service
@RequiredArgsConstructor
public class OrderCouponGateway {
    private final CouponAdaptor couponAdaptor;

    public ItemCouponResult getItemCoupon(Long userId, Long itemCouponId) {
        ItemCouponResponse response = executeGetItemCoupon(userId, itemCouponId);
        return mapItemCouponResult(response);
    }

    private ItemCouponResponse executeGetItemCoupon(Long userId, Long itemCouponId) {
        return executeWithExceptionTranslation(() -> couponAdaptor.getItemCoupon(userId, itemCouponId));
    }

    private ItemCouponResult mapItemCouponResult(ItemCouponResponse response) {
        CouponDiscountPolicy discountPolicy = switch (response.discountType()) {
            case FIXED -> new FixedCouponDiscountPolicy(Money.wons(response.discountAmount()));
            case RATE -> new RateCouponDiscountPolicy(response.discountRate(), Money.wons(response.maxDiscountAmount()));
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

    public CartCouponResult getCartCoupon(Long userId, Long cartCouponId) {
        CartCouponResponse response = executeGetCartCoupon(userId, cartCouponId);
        return mapToCartCouponResult(response);
    }

    private CartCouponResponse executeGetCartCoupon(Long userId, Long cartCouponId) {
        return executeWithExceptionTranslation(() -> couponAdaptor.getCartCoupon(userId, cartCouponId));
    }

    private CartCouponResult mapToCartCouponResult(CartCouponResponse response) {
        CouponDiscountPolicy discountPolicy = switch (response.discountType()) {
            case FIXED -> new FixedCouponDiscountPolicy(Money.wons(response.discountAmount()));
            case RATE -> new RateCouponDiscountPolicy(response.discountRate(), Money.wons(response.maxDiscountAmount()));
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
            throw new DefaultGatewayException(OrderErrorCode.ORDER_COUPON_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_COUPON_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_COUPON_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_COUPON_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        }
    }

    @Deprecated
    public OrderCouponResult.Calculate calculate(OrderCouponCommand.Calculate command) {
        return null;
    }
}
