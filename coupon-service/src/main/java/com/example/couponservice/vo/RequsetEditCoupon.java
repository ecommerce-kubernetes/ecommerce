package com.example.couponservice.vo;

import com.example.common.DiscountType;
import com.example.couponservice.annotation.HasValidDateRange;
import com.example.couponservice.annotation.ValidDateRange;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@ValidDateRange
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequsetEditCoupon implements HasValidDateRange {

    @NotNull(message = "쿠폰ID는 필수 입력값입니다.")
    private Long couponId;

    @NotBlank(message = "쿠폰이름은 필수 입력값입니다.")
    private String name;

    private String description;

    @NotBlank(message = "카테고리는 필수 입력값입니다.")
    private String category;

    @NotNull(message = "할인 타입은 필수 입력값입니다.")
    private DiscountType discountType;

    @NotNull(message = "할인 금액은 필수 입력값입니다.")
    private int discountValue;

    @NotNull(message = "최소사용금액은 필수 입력값입니다.")
    private int minPurchaseAmount;

    @NotNull(message = "최대할인금액은 필수 입력값입니다.")
    private int maxDiscountAmount;

    @NotNull
    @Future(message = "유효 시작일은 현재 시각 이후여야 합니다.")
    private LocalDateTime validFrom;

    @NotNull
    @Future(message = "유효 종료일은 현재 시각 이후여야 합니다.")
    private LocalDateTime validTo;

    @NotNull(message = "중복발급여부는 필수 입력값입니다.")
    private boolean reusable;
}
