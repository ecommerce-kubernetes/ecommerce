package com.example.userservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "배송지 응답 모델")
public class ResponseAddress {

    @Schema(description = "배송지 ID", example = "1")
    private Long addressId; // 배송지 ID

    @Schema(description = "배송지 이름", example = "집")
    private String name; // 배송지이름

    @Schema(description = "주소", example = "수원시 장안구 하율로46")
    private String address;

    @Schema(description = "상세 주소", example = "XX아파트 XXX동 XXX호")
    private String details; //

    @Schema(description = "기본 배송지 여부", example = "true")
    private boolean defaultAddress; // 기본배송지
}
