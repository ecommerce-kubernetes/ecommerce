package com.example.userservice.api.user.controller;

import com.example.userservice.api.user.controller.dto.UserCreateRequest;
import com.example.userservice.api.user.service.UserService;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import com.example.userservice.api.user.service.dto.result.UserCreateResponse;
import com.example.userservice.jpa.entity.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
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
    //비밀번호 확인
    //유저 정보 수정
    //사용자 조회
    //배송지 조회
    //배송지 등록
    //배송지 정보 수정
    //배송지 삭제
    //엑세스 토큰 재발급
    //로그아웃
    //회원탈퇴
}
