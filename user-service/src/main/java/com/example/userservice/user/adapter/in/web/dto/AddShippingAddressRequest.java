package com.example.userservice.user.adapter.in.web.dto;

import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record AddShippingAddressRequest(
        @NotBlank(message = "수령인 이름은 필수 입력값입니다")
        String receiverName,

        @NotBlank(message = "수령인 전화번호는 필수 입력값입니다")
        @Pattern(regexp = "^01[016-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
        String receiverPhone,

        @NotBlank(message = "우편번호는 필수 입력값입니다")
        @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다")
        String zipCode,

        @NotBlank(message = "주소는 필수 입력값입니다")
        String address,

        @NotBlank(message = "상세주소는 필수 입력값입니다")
        String addressDetail
) {
    public AddShippingAddressCommand toCommand(Long userId) {
        return AddShippingAddressCommand.builder()
                .userId(userId)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .build();
    }
}
