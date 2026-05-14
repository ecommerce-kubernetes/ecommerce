package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.domain.model.vo.CouponStatus;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderSheetCouponMapper {
}
