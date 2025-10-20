package com.example.userservice.controller;

import com.example.userservice.advice.exceptions.RefreshTokenNotFoundException;
import com.example.userservice.config.specification.annotation.BadRequestApiResponse;
import com.example.userservice.config.specification.annotation.ConflictApiResponse;
import com.example.userservice.config.specification.annotation.ForbiddenApiResponse;
import com.example.userservice.config.specification.annotation.NotFoundApiResponse;
import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.entity.AddressEntity;
import com.example.userservice.jpa.entity.UserEntity;
import com.example.userservice.service.TokenService;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    //마이페이지 조회

    //회원가입
    @PostMapping
    @Operation(summary = "회원가입", description = "새로운 유저를 등록합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @ConflictApiResponse
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
    @Operation(summary = "비밀번호 확인", description = "유저의 비밀번호가 맞는지 확인합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<?> confirmPassword(@Valid @RequestBody RequestLoginUser user) {
        userService.checkUser(user.getEmail(), user.getPassword());

        return ResponseEntity.ok().build();
    }

    //유저 정보 수정
    @PatchMapping("/update")
    @Operation(summary = "유저 정보 수정", description = "유저 정보를 수정합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<?> updateUser(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestEditUser user) {

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

    @PatchMapping("/update/verify-phoneNumber")
    @Operation(summary = "전화번호 인증", description = "유저의 전화번호를 인증합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<?> verifyPhoneNumberSuccess(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId) {

        userService.verifyPhoneNumber(userId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //사용자 조회
    @GetMapping
    @Operation(summary = "유저 조회", description = "로그인한 해당 유저의 정보를 조회합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> getMyUserData(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId) {

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
                        .cash(userDto.getCash())
                        .point(userDto.getPoint())
                        .build()
        );
    }

    @GetMapping("/balance")
    @Operation(summary = "유저 캐쉬/포인트 조회", description = "로그인한 해당 유저의 캐쉬/포인트를 조회합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUserBalance> getUserBalance(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId){
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(new ResponseUserBalance(userDto.getId(), (long) userDto.getCash(), (long) userDto.getPoint()));
    }

    //마이페이지 조회  { 이름(String),  잔액(Int), 적립금(Int), 주문(Int) - 주문서비스, 장바구니 금액(Int) - 주문서비스, 쿠폰개수(Int) - 쿠폰서비스}
    @GetMapping("/mypage")
    @Operation(summary = "마이페이지 조회", description = "로그인한 해당 유저의 마이페이지를 조회합니다.")
    public ResponseEntity<ResponseUser> getMypage(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId) {

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
    @Operation(summary = "배송지 조회", description = "로그인한 해당 유저의 등록된 배송지를 조회합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> getAddress(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId) {

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

    //배송지 등록
    @PostMapping("/address")
    @Operation(summary = "배송지 등록", description = "로그인한 해당 유저의 배송지를 등록합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    public ResponseEntity<ResponseUser> createAddress(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestCreateAddress address) {

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
    @Operation(summary = "배송지 정보 수정", description = "로그인한 해당 유저의 배송지 정보를 수정합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> updateAddress(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestEditAddress address) {

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
    @Operation(summary = "배송지 삭제", description = "로그인한 해당 유저의 배송지를 삭제합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> deleteAddress(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, @Parameter(description = "주소 ID")@PathVariable("addressId") Long addressId) {

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
    @PatchMapping("/cash/recharge/{amount}")
    @Operation(summary = "유저 캐쉬 충전", description = "로그인한 해당 유저의 캐쉬를 충전합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> rechargeCash(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, @Parameter(description = "금액")@PathVariable("amount") int amount) {
        log.info("요청 응답");
        UserEntity userEntity = userService.rechargeCash(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cash(userEntity.getCash())
                        .build()
        );
    }

    //캐시 차감
    @PatchMapping("/cash/deduct/{amount}")
    @Operation(summary = "유저 캐쉬 차감", description = "로그인한 해당 유저의 캐쉬를 차감합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> deductCash(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, @Parameter(description = "금액")@PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductCash(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cash(userEntity.getCash())
                        .build()
        );
    }

    //포인트 충전
    @PatchMapping("/point/recharge/{amount}")
    @Operation(summary = "유저 포인트 충전", description = "로그인한 해당 유저의 포인트를 충전합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> rechargePoint(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, @Parameter(description = "포인트")@PathVariable("amount") int amount) {

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
    @Operation(summary = "유저 포인트 차감", description = "로그인한 해당 유저의 포인트를 차감합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> deductPoint(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, @Parameter(description = "포인트")@PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductPoint(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }

    //엑세스 토큰 재발급
    @PostMapping("/refresh-token")
    @Operation(summary = "엑세스 토큰 재발급", description = "엑세스 토큰이 만료되었을 때, 리프레시 토큰을 통해 엑세스 토큰을 재발급 받는 API입니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
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
    @Operation(summary = "로그아웃", description = "로그인한 해당 유저의 쿠키에 있는 리프레시 토큰을 지워 로그아웃합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<?> logout(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId, HttpServletResponse response) {
        tokenService.deleteRefreshToken(userId);

        // 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("None")
                .build();

        response.setHeader("Set-Cookie", deleteCookie.toString());
        return ResponseEntity.ok("로그아웃 완료");
    }

    //회원탈퇴
    @DeleteMapping
    @Operation(summary = "회원탈퇴", description = "로그인한 해당 유저 정보를 지워 회원탈퇴합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<?> deleteUser(@Parameter(description = "토큰에서 추출된 유저 ID")@RequestHeader("X-User-Id") Long userId) {

        userService.deleteUser(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
