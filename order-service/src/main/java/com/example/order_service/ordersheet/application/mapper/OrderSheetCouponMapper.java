package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.domain.model.vo.CouponStatus;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderSheetCouponMapper {

    CouponCommand.Calculate toCommand(OrderSheetCommand.CouponCalculate command);

    @Mapping(source = "discountedPrice", target = "price")
    CouponCommand.Item toItemCommand(OrderSheetCommand.AppliedCouponItem command);

    OrderSheetCouponResult.Calculate toResult(CouponClientResponse.Calculate response);
    OrderSheetCouponResult.CartCoupon toCartCouponResult(CouponClientResponse.CartCoupon response);
    OrderSheetCouponResult.ItemCoupon toItemCouponResult(CouponClientResponse.ItemCoupon response);

}
