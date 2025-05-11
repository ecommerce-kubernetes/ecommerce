package com.example.userservice.vo;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestUser {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;\"'<>?,./]).{8,}$",
            message = "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    private String pwd;

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Size(min = 2, max = 12, message = "닉네임은 2글자~12글자 사이여야 합니다.")
    private String name;
}
