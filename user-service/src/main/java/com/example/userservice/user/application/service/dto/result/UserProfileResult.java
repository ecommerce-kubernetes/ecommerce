package com.example.userservice.user.application.service.dto.result;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.user.domain.ShippingAddress;
import com.example.userservice.user.domain.User;
import lombok.Builder;

@Builder
public record UserProfileResult(
        Long userId,
        String userName,
        String phoneNumber,
        Money availablePoints,
        ShippingAddressResult defaultShippingAddress
) {

    @Builder
    public record ShippingAddressResult(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {

        public static ShippingAddressResult from(ShippingAddress shippingAddress) {
            if (shippingAddress == null) {
                return null;
            }
            return ShippingAddressResult.builder()
                    .receiverName(shippingAddress.getReceiverName())
                    .receiverPhone(shippingAddress.getReceiverPhone())
                    .zipCode(shippingAddress.getZipCode())
                    .address(shippingAddress.getAddress())
                    .addressDetail(shippingAddress.getAddressDetail())
                    .build();
        }
    }

    public static UserProfileResult from(User user) {
        return UserProfileResult.builder()
                .userId(user.getId())
                .userName(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .availablePoints(user.getPoint())
                .defaultShippingAddress(ShippingAddressResult.from(user.getDefaultShippingAddress()))
                .build();
    }
}
