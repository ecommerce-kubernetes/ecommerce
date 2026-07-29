package com.example.order_service.order.infrastructure.adaptor.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.port.CouponPortErrorCode;
import com.example.order_service.common.exception.port.DefaultPortException;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponResult;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import org.springframework.stereotype.Component;

@Component
public class OrderCouponPortMapper {

    public ItemCouponResult mapToItemCouponResult(ItemCouponResponse response) {
        CouponDiscountPolicy discountPolicy = createDiscountPolicy(
                response.discountType(),
                response.discountAmount(),
                response.discountRate(),
                response.maxDiscountAmount()
        );

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

    public CartCouponResult mapToCartcouponResult(CartCouponResponse response) {
        CouponDiscountPolicy discountPolicy = createDiscountPolicy(
                response.discountType(),
                response.discountAmount(),
                response.discountRate(),
                response.maxDiscountAmount()
        );

        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(response.cartCouponId(),
                response.name(),
                discountPolicy,
                Money.wons(response.minimumPaymentAmount()));

        return CartCouponResult.builder()
                .cartCoupon(cartCoupon)
                .build();
    }

    private CouponDiscountPolicy createDiscountPolicy(String type, Long amount, Integer rate, Long maxAmount) {
        return switch (type) {
            case "FIXED" -> new FixedCouponDiscountPolicy(Money.wons(amount));
            case "RATE" -> new RateCouponDiscountPolicy(rate, Money.wons(maxAmount));
            default -> throw new DefaultPortException(CouponPortErrorCode.COUPON_CLIENT_ERROR, "UNSUPPORTED_TYPE", "처리할 수 없는 쿠폰 타입입니다.");
        };
    }
}
