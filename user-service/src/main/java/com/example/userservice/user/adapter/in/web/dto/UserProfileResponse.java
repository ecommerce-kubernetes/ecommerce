package com.example.userservice.user.adapter.in.web.dto;

import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import lombok.Builder;

@Builder
public record UserProfileResponse(
        Long userId,
        String userName,
        String phoneNumber,
        Long availablePoints,
        ShippingAddressResponse defaultShippingAddress
) {

    @Builder
    public record ShippingAddressResponse(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
        public static ShippingAddressResponse from(UserProfileResult.ShippingAddressResult result) {
            if (result == null) {
                return null;
            }
            return ShippingAddressResponse.builder()
                    .receiverName(result.receiverName())
                    .receiverPhone(result.receiverPhone())
                    .zipCode(result.zipCode())
                    .address(result.address())
                    .addressDetail(result.addressDetail())
                    .build();
        }
    }

    public static UserProfileResponse from(UserProfileResult result) {
        return UserProfileResponse.builder()
                .userId(result.userId())
                .userName(result.userName())
                .phoneNumber(result.phoneNumber())
                .availablePoints(result.availablePoints().longValue())
                .defaultShippingAddress(ShippingAddressResponse.from(result.defaultShippingAddress()))
                .build();
    }
}
