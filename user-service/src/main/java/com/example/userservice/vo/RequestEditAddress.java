package com.example.userservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "배송지 수정 모델")
public class RequestEditAddress {

    @NotNull(message = "배송지ID는 필수입니다.")
    @Schema(description = "배송지 ID", example = "1")
    private Long addressId;

    @NotBlank(message = "배송지 이름은 필수입니다.")
    @Schema(description = "배송지 이름", example = "집")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    @Schema(description = "주소", example = "수원시 장안구")
    private String address;

    @Schema(description = "상세주소", example = "XX아파트 XXX동 XXX호")
    private String details;

    @NotNull(message = "기본 배송지 여부는 필수입니다.")
    @Schema(description = "기본 배송지 여부", example = "true")
    private boolean defaultAddress;
}
