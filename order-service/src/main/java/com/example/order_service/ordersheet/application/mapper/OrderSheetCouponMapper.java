package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.domain.model.vo.CouponStatus;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderSheetCouponMapper {

    OrderSheetCouponResult.Info toResult(CouponClientResponse.CouponInfo coupon);

    default CouponStatus translateStatus(String status) {
        if (status.equals("SUCCESS")){
            return CouponStatus.SUCCESS;
        } else if (status.equals("EXPIRED")) {
            return CouponStatus.EXPIRED;
        } else if (status.equals("MINIMUM_ORDER_AMOUNT_NOT_MET")) {
            return CouponStatus.MINIMUM_ORDER_AMOUNT_NOT_MET;
        } else {
            return CouponStatus.UNAVAILABLE;
        }
    }
}
