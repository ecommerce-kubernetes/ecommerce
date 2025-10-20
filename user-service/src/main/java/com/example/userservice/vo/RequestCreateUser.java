package com.example.userservice.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원가입 요청 모델")
public class RequestCreateUser {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    @Schema(description = "이메일", example = "test222@naver.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;\"'<>?,./]).{8,}$",
            message = "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    @Schema(description = "비밀번호", example = "Test222!")
    private String pwd;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 12, message = "닉네임은 2글자~12글자 사이여야 합니다.")
    @Schema(description = "닉네임", example = "홍길동")
    private String name;

    @NotNull(message = "생년월일은 필수 입력값입니다.")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "생년월일 형식은 yyyy-MM-dd이어야 합니다.")
    @Schema(description = "생년월일", example = "1999-04-14")
    private String birthDate;

    @NotNull(message = "성별은 필수 입력값입니다.")
    @Pattern(regexp = "MALE|FEMALE", message = "성별은 MALE 또는 FEMALE이어야 합니다.")
    @Schema(description = "성별", example = "MALE")
    private String gender;

    @Schema(description = "핸드폰 번호", example = "01012345678")
    private String phoneNumber;

    @NotNull(message = "핸드폰 번호 인증여부는 필수입니다.")
    @Schema(description = "핸드폰 번호 인증여부", example = "false")
    private boolean phoneVerified;
}
