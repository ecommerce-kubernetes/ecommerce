package com.example.order_service.order.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.dto.result.OrderUserResult;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderUserMapper {

    @Mapping(source = "pointBalance", target = "availablePoints")
    @Mapping(source = "userName", target = "ordererName")
    @Mapping(source = "phoneNumber", target = "ordererPhone")
    OrderUserResult.OrdererInfo toResult(UserClientResponse.UserInfo user);

    OrderUserResult.UserPoint toResult(UserClientResponse.UserPoints points);
}
