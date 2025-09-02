package com.example.userservice.controller;

import com.example.userservice.advice.exceptions.RefreshTokenNotFoundException;
import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.entity.AddressEntity;
import com.example.userservice.jpa.entity.UserEntity;
import com.example.userservice.service.TokenService;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/V1__create_users_table.sql")
@Slf4j
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    //회원가입
    @PostMapping
    public ResponseEntity<ResponseUser> createUser(@Valid @RequestBody RequestCreateUser user) {

        UserDto userDto = UserDto.builder()
                .email(user.getEmail())
                .pwd(user.getPwd())
                .name(user.getName())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .phoneVerified(user.isPhoneVerified())
                .build();

        UserEntity createUser = userService.createUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUser.builder()
                        .email(createUser.getEmail())
                        .name(createUser.getName())
                        .birthDate(String.valueOf(createUser.getBirthDate()))
                        .gender(String.valueOf(createUser.getGender()))
                        .phoneNumber(createUser.getPhoneNumber())
                        .phoneVerified(createUser.isPhoneVerified())
                        .role(String.valueOf(createUser.getRole()))
                        .build()
        );
    }

    //아이디 찾기

    //비밀번호 찾기

    //비밀번호 확인
    @PostMapping("/confirm-password")
    public ResponseEntity<?> confirmPassword(@Valid @RequestBody RequestLoginUser user) {
        userService.checkUser(user.getEmail(), user.getPassword());

        return ResponseEntity.ok().build();
    }

    //유저 정보 수정
    @PatchMapping("/update")
    public ResponseEntity<?> updateUser(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestEditUser user) {

        UserDto editUserData = UserDto.builder()
                .id(userId)
                .name(user.getName())
                .pwd(user.getPwd())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .build();

        UserEntity userEntity = userService.updateUser(editUserData);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //전화번호 인증 완료
    @PatchMapping("/update/verify-phoneNumber")
    public ResponseEntity<?> verifyPhoneNumberSuccess(@RequestHeader("X-User-Id") Long userId) {

        userService.verifyPhoneNumber(userId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //사용자 조회
    @GetMapping
    public ResponseEntity<ResponseUser> getMyUserData(@RequestHeader("X-User-Id") Long userId) {

        UserDto userDto = userService.getUserById(userId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userDto.getId())
                        .email(userDto.getEmail())
                        .name(userDto.getName())
                        .gender(userDto.getGender())
                        .birthDate(userDto.getBirthDate())
                        .phoneNumber(userDto.getPhoneNumber())
                        .phoneVerified(userDto.isPhoneVerified())
                        .createdAt(userDto.getCreatedAt())
                        .cache(userDto.getCache())
                        .point(userDto.getPoint())
                        .build()
        );
    }

    //마이페이지 조회  { 이름(String),  잔액(Int), 적립금(Int), 주문(Int) - 주문서비스, 장바구니 금액(Int) - 주문서비스, 쿠폰개수(Int) - 쿠폰서비스}
    @GetMapping("/mypage")
    public ResponseEntity<ResponseUser> getMypage(@RequestHeader("X-User-Id") Long userId) {

        userService.getMypage(userId);

        List<AddressEntity> addresses = userService.getAddressesByUserId(userId);

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


    //배송지 조회
    @GetMapping("/address")
    public ResponseEntity<ResponseUser> getAddress(@RequestHeader("X-User-Id") Long userId) {

        List<AddressEntity> addresses = userService.getAddressesByUserId(userId);

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
    public ResponseEntity<ResponseUser> createAddress(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestCreateAddress address) {

        AddressDto addressDto = AddressDto.builder()
                .name(address.getName())
                .address(address.getAddress())
                .details(address.getDetails())
                .defaultAddress(address.isDefaultAddress())
                .build();

        UserEntity userEntity = userService.addAddressByUserId(userId, addressDto);

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
    public ResponseEntity<ResponseUser> updateAddress(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestEditAddress address) {

        AddressDto addressDto = AddressDto.builder()
                .addressId(address.getAddressId())
                .name(address.getName())
                .address(address.getAddress())
                .details(address.getDetails())
                .defaultAddress(address.isDefaultAddress())
                .build();

        UserEntity userEntity = userService.updateAddress(userId, addressDto);

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
    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<ResponseUser> deleteAddress(@RequestHeader("X-User-Id") Long userId, @PathVariable("addressId") Long addressId) {

        UserEntity userEntity = userService.deleteAddress(userId, addressId);

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
    public ResponseEntity<ResponseUser> rechargeCache(@RequestHeader("X-User-Id") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.rechargeCache(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cache(userEntity.getCache())
                        .build()
        );
    }

    //캐시 차감
    @PatchMapping("/cache/deduct/{amount}")
    public ResponseEntity<ResponseUser> deductCache(@RequestHeader("X-User-Id") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductCache(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cache(userEntity.getCache())
                        .build()
        );
    }

    //포인트 충전
    @PatchMapping("/point/recharge/{amount}")
    public ResponseEntity<ResponseUser> rechargePoint(@RequestHeader("X-User-Id") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.rechargePoint(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }

    //포인트 차감
    @PatchMapping("/point/deduct/{amount}")
    public ResponseEntity<ResponseUser> deductPoint(@RequestHeader("X-User-Id") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductPoint(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }

    //캐시, 포인트 확인
    @GetMapping("/validation")
    public ResponseEntity<?> validPointAndCache(
            @RequestBody RequestValidCache requestValidCache) {

        userService.validPointAndCache(
                requestValidCache.getUserId(),
                requestValidCache.getReservedPointAmount(),
                requestValidCache.getReservedCacheAmount());

        return ResponseEntity.status(HttpStatus.OK).build();
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
    public ResponseEntity<?> logout(@RequestHeader("X-User-Id") Long userId, HttpServletResponse response) {
        tokenService.deleteRefreshToken(userId);

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
    public ResponseEntity<?> deleteUser(@RequestHeader("X-User-Id") Long userId) {

        userService.deleteUser(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
