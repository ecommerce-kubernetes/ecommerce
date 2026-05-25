package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderUserMapper {

    @Mapping(source = "defaultShippingAddress", target = "shippingAddress")
    OrderUserResult.Profile toResult(UserClientResponse.Profile profile);
    OrderUserResult.ShippingAddress toShippingAddressResult(UserClientResponse.ShippingAddress shippingAddress);
    OrderUserResult.UserPoint toResult(UserClientResponse.UserPoints points);
}
