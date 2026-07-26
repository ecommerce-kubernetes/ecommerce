package com.example.order_service.order.infrastructure.adaptor.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.user.UserPointsResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.order.application.port.dto.result.OrdererPointResult;
import com.example.order_service.order.application.port.dto.result.OrdererProfileResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import org.springframework.stereotype.Component;

@Component
public class OrderUserPortMapper {

    public OrdererProfileResult mapToOrdererProfileResult(UserProfileResponse response) {
        Orderer orderer = Orderer.of(response.userId(), response.userName(), response.phoneNumber());
        ShippingAddress shippingAddress = mapToShippingAddress(response.defaultShippingAddress());
        Money availablePoints = mapToAvailablePoints(response.availablePoints());
        return OrdererProfileResult.builder()
                .orderer(orderer)
                .availablePoints(availablePoints)
                .defaultShippingAddress(shippingAddress)
                .build();
    }

    private ShippingAddress mapToShippingAddress(UserProfileResponse.ShippingAddressResponse response) {
        if (response == null) {
            return null;
        }
        return ShippingAddress.of(response.receiverName(), response.receiverPhone(), response.zipCode(), response.address(),
                response.addressDetail());
    }

    private Money mapToAvailablePoints(Long availablePoints) {
        if (availablePoints == null) {
            return Money.ZERO;
        }
        return Money.wons(availablePoints);
    }

    public OrdererPointResult mapToOrdererPointResult(UserPointsResponse response){
        Money availablePoints = mapToAvailablePoints(response.availablePoints());
        return OrdererPointResult.builder()
                .userId(response.userId())
                .availablePoints(availablePoints)
                .build();
    }
}
