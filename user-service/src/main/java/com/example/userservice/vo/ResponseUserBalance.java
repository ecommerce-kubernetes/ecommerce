package com.example.userservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "유저 캐쉬/포인트 조회 응답 모델")
public class ResponseUserBalance {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "캐쉬", example = "200000")
    private Long cashAmount;

    @Schema(description = "포인트", example = "10000")
    private Long pointAmount;

}
