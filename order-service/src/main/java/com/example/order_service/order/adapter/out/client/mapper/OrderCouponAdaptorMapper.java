package com.example.order_service.order.adapter.out.client.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.application.port.dto.OrderCouponStatus;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import com.example.order_service.order.exception.OrderCouponPortErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderCouponAdaptorMapper {

    public ItemCouponsResult mapToItemCouponsResult(ItemCouponsResponse response) {
        List<ItemCouponsResult.ItemCouponResult> itemCoupons = response.itemCoupons().stream().map(this::mapToItemCouponResult).toList();
        return ItemCouponsResult.builder()
                .userId(response.userId())
                .itemCoupons(itemCoupons)
                .build();
    }

    private ItemCouponsResult.ItemCouponResult mapToItemCouponResult(ItemCouponsResponse.ItemCoupon itemCoupon) {
        CouponDiscountPolicy discountPolicy = createDiscountPolicy(
                itemCoupon.discountType(),
                itemCoupon.discountAmount(),
                itemCoupon.discountRate(),
                itemCoupon.maxDiscountAmount()
        );

        ItemCouponSnapshot itemCouponSnapshot = ItemCouponSnapshot.of(
                itemCoupon.itemCouponId(),
                itemCoupon.name(),
                discountPolicy,
                itemCoupon.applyQuantityLimit()
        );

        OrderCouponStatus status = mapToCouponStatus(itemCoupon.status());

        return ItemCouponsResult.ItemCouponResult.builder()
                .status(status)
                .itemCoupon(itemCouponSnapshot)
                .expiresAt(itemCoupon.expiresAt())
                .build();
    }

    public CartCouponResult mapToCartCouponResult(CartCouponResponse response) {
        CouponDiscountPolicy discountPolicy = createDiscountPolicy(
                response.discountType(),
                response.discountAmount(),
                response.discountRate(),
                response.maxDiscountAmount()
        );

        OrderCouponStatus status = mapToCouponStatus(response.status());

        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(response.cartCouponId(),
                response.name(),
                discountPolicy,
                Money.wons(response.minimumPaymentAmount()));

        return CartCouponResult.builder()
                .status(status)
                .cartCoupon(cartCoupon)
                .expiresAt(response.expiresAt())
                .build();
    }

    private CouponDiscountPolicy createDiscountPolicy(String type, Long amount, Integer rate, Long maxAmount) {
        return switch (type) {
            case "FIXED" -> new FixedCouponDiscountPolicy(Money.wons(amount));
            case "RATE" -> new RateCouponDiscountPolicy(rate, Money.wons(maxAmount));
            default -> throw new PortException(OrderCouponPortErrorCode.COUPON_CLIENT_ERROR, "UNSUPPORTED_TYPE", "처리할 수 없는 쿠폰 타입입니다.");
        };
    }

    private OrderCouponStatus mapToCouponStatus(String couponStatus) {
        return switch (couponStatus) {
            case "AVAILABLE" -> OrderCouponStatus.AVAILABLE;
            case "USED" -> OrderCouponStatus.USED;
            case null, default -> OrderCouponStatus.UNKNOWN;
        };
    }
}
