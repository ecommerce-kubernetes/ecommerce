package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderCouponMapper {

    CouponCommand.Calculate toCommand(OrderCouponCommand.Calculate command);
    @Mapping(source = "discountedPrice", target = "price")
    CouponCommand.Item toItemCommand(OrderCouponCommand.AppliedCouponItem command);

    OrderCouponResult.Calculate toResult(CouponClientResponse.Calculate response);
    OrderCouponSnapshot toCartCouponSnapshot(CouponClientResponse.CartCoupon response);
    @Mapping(source = ".", target = "itemCoupon")
    OrderCouponResult.ItemCoupon toItemCouponResult(CouponClientResponse.ItemCoupon response);
    OrderCouponSnapshot toItemCouponSnapshot(CouponClientResponse.ItemCoupon response);
}
