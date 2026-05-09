package com.example.order_service.order.application.mapper;

import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-09T19:29:54+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderCouponMapperImpl implements OrderCouponMapper {

    @Override
    public OrderCouponResult.CouponValidation toResult(CouponClientResponse.Calculate coupon) {
        if ( coupon == null ) {
            return null;
        }

        OrderCouponResult.CouponValidation.CouponValidationBuilder couponValidation = OrderCouponResult.CouponValidation.builder();

        couponValidation.status( translateStatus( coupon.code() ) );
        couponValidation.couponBenefit( toBenefit( coupon.discountBenefit() ) );

        return couponValidation.build();
    }

    @Override
    public OrderCouponResult.CouponBenefit toBenefit(CouponClientResponse.DiscountBenefit benefit) {
        if ( benefit == null ) {
            return null;
        }

        OrderCouponResult.CouponBenefit.CouponBenefitBuilder couponBenefit = OrderCouponResult.CouponBenefit.builder();

        couponBenefit.couponId( benefit.couponId() );
        couponBenefit.couponName( benefit.couponName() );
        couponBenefit.discountAmount( benefit.discountAmount() );

        return couponBenefit.build();
    }
}
