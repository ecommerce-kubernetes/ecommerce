package com.example.userservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "유저 응답 모델")
public class ResponseUser {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "test222@naver.com")
    private String email;

    @Schema(description = "닉네임", example = "홍길동")
    private String name;

    @Schema(description = "생년월일", example = "1999-04-14")
    private String birthDate;

    @Schema(description = "성별", example = "MALE")
    private String gender;

    @Schema(description = "핸드폰 번호", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "핸드폰 번호 인증여부", example = "false")
    private Boolean phoneVerified;

    @Schema(description = "생성 날짜", example = "2025-10-18T14:35:50")
    private LocalDateTime createdAt;

    @Schema(description = "배송지", example = "집, 회사")
    private List<ResponseAddress> addresses;

    @Schema(description = "캐쉬", example = "200000")
    private Integer cash;

    @Schema(description = "포인트", example = "10000")
    private Integer point;

    @Schema(description = "역할", example = "ROLE_ADMIN")
    private String role;

    @Builder
    public ResponseUser(Long userId, String email, String name, String birthDate, String gender, String phoneNumber, Boolean phoneVerified, LocalDateTime createdAt, List<ResponseAddress> addresses, Integer cash, Integer point, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.phoneVerified = phoneVerified;
        this.createdAt = createdAt;
        this.addresses = addresses;
        this.cash = cash;
        this.point = point;
        this.role = role;
    }
}
