package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderUserMapper {

    @Mapping(source = "defaultShippingAddress", target = "shippingAddress")
    @Mapping(source = ".", target = "orderer")
    OrderUserResult.Profile toResult(UserClientResponse.Profile profile);
    OrderUserResult.UserPoint toResult(UserClientResponse.UserPoints points);

    default Orderer toOrderer(UserClientResponse.Profile profile) {
        if (profile == null) {
            return null;
        }
        return Orderer.of(profile.userId(), profile.userName(), profile.phoneNumber());
    }

    default ShippingAddress toShippingAddress(UserClientResponse.ShippingAddress address) {
        if (address == null) {
            return null;
        }
        return ShippingAddress.of(
                address.receiverName(),
                address.receiverPhone(),
                address.zipCode(),
                address.address(),
                address.addressDetail()
        );
    }
}
