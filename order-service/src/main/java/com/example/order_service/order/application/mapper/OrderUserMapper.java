package com.example.order_service.order.application.mapper;

import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.dto.result.OrderUserResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderUserMapper {

    @Mapping(source = "pointBalance", target = "availablePoints")
    @Mapping(source = "userName", target = "ordererName")
    @Mapping(source = "phoneNumber", target = "ordererPhone")
    OrderUserResult.OrdererInfo toResult(UserClientResponse.UserInfo user);
}
