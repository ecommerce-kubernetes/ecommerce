package com.example.userservice.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestAddress {

    @NotNull(message = "배송지ID는 필수입니다.")
    private Long addressId;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    private String details;

    @NotNull(message = "기본 배송지 여부는 필수입니다.")
    private boolean defaultAddress;
}
