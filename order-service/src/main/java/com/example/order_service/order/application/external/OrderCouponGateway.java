package com.example.order_service.order.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.result.ItemCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.mapper.OrderCouponMapper;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final OrderCouponMapper mapper;

    @Deprecated
    public OrderCouponResult.Calculate calculate(OrderCouponCommand.Calculate command) {
        CouponCommand.Calculate couponCommand = mapper.toCommand(command);
        CouponClientResponse.Calculate response = fetchCouponWithTranslation(couponCommand);
        return mapper.toResult(response);
    }

    public ItemCouponResult getItemCoupon(Long userId, Long itemCouponId) {
        ItemCouponResponse response = executeGetItemCoupon(userId, itemCouponId);
        return mapItemCouponResult(response);
    }

    private ItemCouponResult mapItemCouponResult(ItemCouponResponse response) {

        if (response.discountType() == ItemCouponResponse.DiscountType.FIXED) {
            ItemCouponSnapshot itemCouponSnapshot = ItemCouponSnapshot.of(response.itemCouponId(), response.name(),
                    new FixedCouponDiscountPolicy(Money.wons(response.discountAmount())), response.applyQuantityLimit());
            return ItemCouponResult.builder()
                    .itemCoupon(itemCouponSnapshot)
                    .build();
        }
        ItemCouponSnapshot itemCouponSnapshot = ItemCouponSnapshot.of(response.itemCouponId(), response.name(),
                new RateCouponDiscountPolicy(response.discountRate(), Money.wons(response.maxDiscountAmount())), response.applyQuantityLimit());
        return ItemCouponResult.builder()
                .itemCoupon(itemCouponSnapshot)
                .build();
    }

    private ItemCouponResponse executeGetItemCoupon(Long userId, Long itemCouponId) {
        try {
            return couponAdaptor.getItemCoupon(userId, itemCouponId);
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

    private CouponClientResponse.Calculate fetchCouponWithTranslation(CouponCommand.Calculate command) {
        try {
            return couponAdaptor.calculate(command);
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
}
