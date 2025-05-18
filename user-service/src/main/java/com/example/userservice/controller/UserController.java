package com.example.userservice.controller;

import com.example.userservice.advice.exceptions.RefreshTokenNotFoundException;
import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.AddressEntity;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.TokenService;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private UserService userService;
    private TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    //회원가입
    @PostMapping
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestCreateUser user) {

        UserDto userDto = UserDto.builder()
                .email(user.getEmail())
                .pwd(user.getPwd())
                .name(user.getName())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .build();

        UserEntity createUser = userService.createUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUser.builder()
                        .email(createUser.getEmail())
                        .name(createUser.getName())
                        .birthDate(String.valueOf(createUser.getBirthDate()))
                        .gender(String.valueOf(createUser.getGender()))
                        .phoneNumber(createUser.getPhoneNumber())
                        .role(String.valueOf(createUser.getRole()))
                        .build()
        );
    }

    //비밀번호 확인
    @PostMapping("/confirm-password")
    public ResponseEntity<?> confirmPassword(@RequestBody RequestLoginUser user) {
        userService.checkUser(user.getEmail(), user.getPassword());

        return ResponseEntity.ok().build();
    }

    //유저 정보 수정
    @PatchMapping
    public ResponseEntity<?> updateUser(@RequestHeader("X-User-Id") String userId, @RequestBody RequestEditUser user) {

        UserDto editUserData = UserDto.builder()
                .id(Long.valueOf(userId))
                .name(user.getName())
                .pwd(user.getPwd())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .build();

        UserEntity userEntity = userService.updateUser(editUserData);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //마이페이지 조회  { 이름(String),  잔액(Int), 적립금(Int), 주문(Int) - 주문서비스, 장바구니 금액(Int) - 주문서비스, 쿠폰개수(Int) - 쿠폰서비스}

    //배송지 조회
    @GetMapping("/address")
    public ResponseEntity<ResponseUser> createAddress(@RequestHeader("X-User-Id") String userId) {

        List<AddressEntity> addresses = userService.getAddressesByUserId(Long.valueOf(userId));

        List<ResponseAddress> responseAddressList = addresses.stream()
                .map(v -> ResponseAddress.builder()
                        .addressId(v.getId())
                        .name(v.getName())
                        .address(v.getAddress())
                        .details(v.getDetails())
                        .defaultAddress(v.isDefaultAddress())
                        .build())
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(Long.valueOf(userId))
                        .addresses(responseAddressList)
                        .build()
        );
    }

    //배송지 추가
    @PostMapping("/address")
    public ResponseEntity<ResponseUser> createAddress(@RequestHeader("X-User-Id") String userId, @RequestBody RequestAddress address) {

        AddressDto addressDto = AddressDto.builder()
                .name(address.getName())
                .address(address.getAddress())
                .details(address.getDetails())
                .defaultAddress(address.isDefaultAddress())
                .build();

        UserEntity userEntity = userService.addAddressByUserId(Long.valueOf(userId), addressDto);

        List<ResponseAddress> responseAddressList = userEntity.getAddresses().stream()
                .map(v -> ResponseAddress.builder()
                        .addressId(v.getId())
                        .name(v.getName())
                        .address(v.getAddress())
                        .details(v.getDetails())
                        .defaultAddress(v.isDefaultAddress())
                        .build())
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .addresses(responseAddressList)
                        .build()
        );
    }

    //배송지 정보 수정
    @PatchMapping("/address")
    public ResponseEntity<ResponseUser> updateAddress(@RequestHeader("X-User-Id") String userId, @RequestBody RequestAddress address) {

        AddressDto addressDto = AddressDto.builder()
                .name(address.getName())
                .address(address.getAddress())
                .details(address.getDetails())
                .defaultAddress(address.isDefaultAddress())
                .build();

        UserEntity userEntity = userService.updateAddress(Long.valueOf(userId), addressDto);

        List<ResponseAddress> responseAddressList = userEntity.getAddresses().stream()
                .map(v -> ResponseAddress.builder()
                        .addressId(v.getId())
                        .name(v.getName())
                        .address(v.getAddress())
                        .details(v.getDetails())
                        .defaultAddress(v.isDefaultAddress())
                        .build())
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .email(userEntity.getEmail())
                        .addresses(responseAddressList)
                        .build()
        );
    }

    //배송지 삭제
    @DeleteMapping("/address/{addressName}")
    public ResponseEntity<ResponseUser> deleteAddress(@RequestHeader("X-User-Id") String userId, @PathVariable("addressName") String addressName) {

        UserEntity userEntity = userService.deleteAddress(Long.valueOf(userId), addressName);

        List<ResponseAddress> responseAddressList = userEntity.getAddresses().stream()
                .map(v -> ResponseAddress.builder()
                        .addressId(v.getId())
                        .name(v.getName())
                        .address(v.getAddress())
                        .details(v.getDetails())
                        .defaultAddress(v.isDefaultAddress())
                        .build())
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .addresses(responseAddressList)
                        .build()
        );
    }

    //캐시 충전
    @PatchMapping("/cache/recharge/{amount}")
    public ResponseEntity<ResponseUser> rechargeCache(@RequestHeader("X-User-Id") String userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.rechargeCache(Long.valueOf(userId), amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cache(userEntity.getCache())
                        .build()
        );
    }

    //캐시 차감
    @PatchMapping("/cache/deduct/{amount}")
    public ResponseEntity<ResponseUser> deductCache(@RequestHeader("X-User-Id") String userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductCache(Long.valueOf(userId), amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cache(userEntity.getCache())
                        .build()
        );
    }

    //포인트 충전
    @PatchMapping("/point/recharge/{amount}")
    public ResponseEntity<ResponseUser> rechargePoint(@RequestHeader("X-User-Id") String userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.rechargePoint(Long.valueOf(userId), amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }

    //포인트 차감
    @PatchMapping("/point/deduct/{amount}")
    public ResponseEntity<ResponseUser> deductPoint(@RequestHeader("X-User-Id") String userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductPoint(Long.valueOf(userId), amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }

    //엑세스 토큰 재발급
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        //쿠키에서 refresh_token 가져오기
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new RefreshTokenNotFoundException("리프레시 토큰이 없습니다.");
        }

        String newAccessToken = tokenService.reissueAccessToken(refreshToken);

        return ResponseEntity.ok().header("token", newAccessToken).build();
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("X-User-Id") String userId, HttpServletResponse response) {
        tokenService.deleteRefreshToken(Long.valueOf(userId));

        // 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", deleteCookie.toString());
        return ResponseEntity.ok("로그아웃 완료");
    }

    //회원탈퇴
    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestHeader("X-User-Id") String userId) {

        userService.deleteUser(Long.valueOf(userId));

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
