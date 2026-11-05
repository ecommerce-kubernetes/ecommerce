package com.example.userservice.user.adapter.in.web.dto;

import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record AddShippingAddressRequest(
        @NotBlank(message = "{user.shippingAddress.receiverName.notBlank}")
        String receiverName,

        @NotBlank(message = "{user.shippingAddress.receiverPhone.notBlank}")
        @Pattern(regexp = "^01[016-9]-\\d{3,4}-\\d{4}$", message = "{user.phoneNumber.pattern}")
        String receiverPhone,

        @NotBlank(message = "{user.shippingAddress.zipCode.notBlank}")
        @Pattern(regexp = "^[0-9]{5}$", message = "{user.shippingAddress.zipCode.pattern}")
        String zipCode,

        @NotBlank(message = "{user.shippingAddress.address.notBlank}")
        String address,

        @NotBlank(message = "{user.shippingAddress.addressDetail.notBlank}")
        String addressDetail,

        boolean isDefault
) {
    public AddShippingAddressCommand toCommand(Long userId) {
        return AddShippingAddressCommand.builder()
                .userId(userId)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .isDefault(isDefault)
                .build();
    }
}
