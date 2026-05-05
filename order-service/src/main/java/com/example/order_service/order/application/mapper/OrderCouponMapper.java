package com.example.order_service.order.application.mapper;

import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.domain.model.vo.CouponValidationStatus;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderCouponMapper {

    @Mapping(source = "code", target = "status")
    OrderCouponResult.CouponValidation toResult(CouponClientResponse.Calculate coupon);
    OrderCouponResult.CouponBenefit toBenefit(CouponClientResponse.DiscountBenefit benefit);

    default CouponValidationStatus translateStatus(String status) {
        if (status == null) {
            return CouponValidationStatus.UNAVAILABLE;
        }

        return switch (status) {
            case "SUCCESS" -> CouponValidationStatus.SUCCESS;
            case "MINIMUM_AMOUNT_NOT_MET" -> CouponValidationStatus.MINIMUM_AMOUNT_NOT_MET;
            case "EXPIRED" -> CouponValidationStatus.EXPIRED;
            default -> CouponValidationStatus.UNAVAILABLE;
        };
    }
}
