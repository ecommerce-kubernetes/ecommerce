package com.example.order_service.order.api.web.dto.ordersheet.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.UpdateOrderSheetShippingAddressCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UpdateOrderSheetShippingAddressRequest(
        @NotBlank(message = "{orderSheet.receiverName.notNull}")
        String receiverName,
        @Pattern(regexp = "^01[016-9]-\\d{3,4}-\\d{4}$", message = "{orderSheet.receiverPhone.pattern}")
        @NotBlank(message = "{orderSheet.receiverPhone.notNull}")
        String receiverPhone,
        @NotBlank(message = "{orderSheet.zipCode.notNull}")
        @Pattern(regexp = "^[0-9]{5}$", message = "{orderSheet.zipCode.pattern}")
        String zipCode,
        @NotBlank(message = "{orderSheet.address.notNull}")
        String address,
        @NotBlank(message = "{orderSheet.addressDetail.notNull}")
        String addressDetail
) {
        public UpdateOrderSheetShippingAddressCommand toCommand(Long orderSheetId, Long userId) {
                return UpdateOrderSheetShippingAddressCommand.builder()
                        .orderSheetId(orderSheetId)
                        .userId(userId)
                        .receiverName(receiverName)
                        .receiverPhone(receiverPhone)
                        .zipCode(zipCode)
                        .address(address)
                        .addressDetail(addressDetail)
                        .build();
        }
}
