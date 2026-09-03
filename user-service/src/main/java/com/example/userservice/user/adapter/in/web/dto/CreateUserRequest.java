package com.example.userservice.user.adapter.in.web.dto;

import com.example.userservice.user.application.service.dto.command.CreateUserCommand;
import com.example.userservice.user.domain.vo.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CreateUserRequest {
    @NotBlank(message = "{user.email.notBlank}")
    @Email(message = "{user.email.pattern}")
    private String email;

    @NotBlank(message = "{user.password.notBlank}")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;\"'<>?,./]).{8,}$",
            message = "{user.password.pattern}"
    )
    private String password;

    @NotBlank(message = "{user.name.notBlank}")
    @Size(min = 2, max = 12, message = "{user.name.size}")
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NotNull(message = "{user.birthDate.notNull}")
    private LocalDate birthDate;

    @NotNull(message = "{user.gender.notNull}")
    @Pattern(regexp = "MALE|FEMALE", message = "{user.gender.pattern}")
    private String gender;

    @NotBlank(message = "{user.phoneNumber.notBlank}")
    @Pattern(regexp = "^01[016-9]-\\d{3,4}-\\d{4}$", message = "{user.phoneNumber.pattern}")
    private String phoneNumber;

    public CreateUserCommand toCommand() {
        return CreateUserCommand.builder()
                .email(email)
                .password(password)
                .name(name)
                .birthDate(birthDate)
                .gender(Gender.from(gender))
                .phoneNumber(phoneNumber)
                .build();
    }
}
