package com.example.order_service.order.application.mapper;

import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import com.example.order_service.order.domain.model.vo.CouponValidationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderCouponMapper {

}
