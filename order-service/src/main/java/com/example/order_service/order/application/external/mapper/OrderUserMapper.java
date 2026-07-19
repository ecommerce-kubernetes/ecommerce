package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.external.dto.result.OrdererProfileResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderUserMapper {

    @Mapping(source = ".", target = "orderer")
    @Mapping(source = "availablePoints", target = "availablePoints")
    OrdererProfileResult toOrdererProfileResult(UserProfileResponse response);

    default Orderer toOrderer(UserProfileResponse response) {
        if (response == null) {
            return null;
        }

        return Orderer.of(response.userId(), response.userName(), response.phoneNumber());
    }

    default ShippingAddress toShippingAddress(UserProfileResponse.ShippingAddressResponse address) {
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

    @Mapping(source = "defaultShippingInfo", target = "shippingAddress")
    @Mapping(source = ".", target = "orderer")
    OrderUserResult.Profile toResult(UserClientResponse.Profile profile);
    OrderUserResult.UserPoint toResult(UserClientResponse.UserPoints points);

    @Deprecated
    default Orderer toOrderer(UserClientResponse.Profile profile) {
        if (profile == null) {
            return null;
        }
        return Orderer.of(profile.userId(), profile.userName(), profile.phoneNumber());
    }

    @Deprecated
    default ShippingAddress toShippingAddress(UserClientResponse.ShippingInfo address) {
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
