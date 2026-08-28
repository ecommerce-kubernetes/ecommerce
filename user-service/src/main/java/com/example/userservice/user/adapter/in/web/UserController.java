package com.example.userservice.user.adapter.in.web;

import com.example.userservice.user.adapter.in.web.dto.EmailAvailableResponse;
import com.example.userservice.user.adapter.in.web.dto.UserCreateRequest;
import com.example.userservice.user.domain.model.Gender;
import com.example.userservice.user.application.service.UserService;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.UserCreateResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserCreateResponse> createUser(@Validated @RequestBody UserCreateRequest request) {
        UserCreateCommand command = UserCreateCommand.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(Gender.from(request.getGender()))
                .phoneNumber(request.getPhoneNumber())
                .build();
        UserCreateResponse response = userService.createUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/email-availability")
    public ResponseEntity<EmailAvailableResponse> checkEmailAvailable(@RequestParam(name = "email")
                                                                          @NotBlank(message = "email 파라미터는 필수값 입니다")
                                                                          @Email(message = "올바른 이메일 형식이 아닙니다") String email){
        EmailAvailableResponse response = userService.checkAvailableEmail(email);
        return ResponseEntity.ok(response);
    }

    //배송지 추가
    //배송지 삭제
}
