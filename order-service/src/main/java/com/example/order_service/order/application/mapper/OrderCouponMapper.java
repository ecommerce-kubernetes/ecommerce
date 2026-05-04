package com.example.order_service.order.application.mapper;

import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.domain.model.vo.CouponValidationStatus;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = CouponValidationStatus.class)
public interface OrderCouponMapper {

    @Mapping(target = "status", expression = "java(CouponValidationStatus.from(coupon.code()))")
    OrderCouponResult.CouponValidation toResult(CouponClientResponse.Calculate coupon);
    OrderCouponResult.CouponBenefit toBenefit(CouponClientResponse.DiscountBenefit benefit);
}
