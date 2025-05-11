package com.example.userservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressDto {

    private Long addressId; // 배송지 ID
    private String name; // 배송지이름
    private String address; // 도로명 주소
    private String details; // 상세 주소
    private boolean defaultAddress; // 기본배송지
}
