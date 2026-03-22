package com.example.userservice.api.user.controller;

import com.example.userservice.api.user.controller.dto.EmailAvailableResponse;
import com.example.userservice.api.user.controller.dto.UserCreateRequest;
import com.example.userservice.api.user.domain.model.Gender;
import com.example.userservice.api.user.service.UserService;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import com.example.userservice.api.user.service.dto.result.UserCreateResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    //TODO
    //전체 유저 조회
    //유저아이디로 유저 조회
    //유저 정보 수정
    //유저 삭제
    //배송지 추가
    //배송지 정보 수정
    //배송지 삭제
    //포인트 충전
    //포인트 차감
    //아이디 찾기
    //비밀번호 찾기
    //유저 정보 수정
    //사용자 조회
    //배송지 조회
    //배송지 등록
    //배송지 정보 수정
    //배송지 삭제
    //엑세스 토큰 재발급
    //회원탈퇴
}
