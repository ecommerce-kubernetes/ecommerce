package com.example.userservice.vo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class ResponseAddress {

    private Long addressId; // 배송지 ID
    private String name; // 배송지이름
    private String address; // 도로명 주소
    private String details; // 상세 주소
    private boolean defaultAddress; // 기본배송지
}
