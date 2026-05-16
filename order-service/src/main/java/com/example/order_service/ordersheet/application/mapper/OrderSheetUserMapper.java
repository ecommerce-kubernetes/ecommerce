package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderSheetUserMapper {
    @Mapping(source = "defaultShippingAddress", target = "shippingAddress")
    OrderSheetUserResult.Profile toResult(UserClientResponse.Profile profile);
    OrderSheetUserResult.ShippingAddress toShippingAddressResult(UserClientResponse.ShippingAddress shippingAddress);
}
