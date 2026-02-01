package com.example.userservice.api.user.controller.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserCreateRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다")
    @Email(message = "올바른 이메일 형식을 입력해주세요")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;\"'<>?,./]).{8,}$",
            message = "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다"
    )
    private String password;

    @NotBlank(message = "이름은 필수 입력값입니다")
    @Size(min = 2, max = 12, message = "이름은 2글자~12글자 사이여야 합니다")
    private String name;

    @NotNull(message = "생년월일은 필수 입력값입니다")
    private LocalDate birthDate;

    @NotNull(message = "성별은 필수 입력값입니다")
    @Pattern(regexp = "MALE|FEMALE", message = "성별은 MALE 또는 FEMALE 이어야 합니다")
    private String gender;

    @NotBlank(message = "전화번호는 필수 입력값 입니다")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    private String phoneNumber;
}
