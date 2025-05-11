package com.example.userservice.controller;

import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestAddress;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseAddress;
import com.example.userservice.vo.ResponseUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome";
    }

    //회원가입
    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {

        UserDto userDto = UserDto.builder()
                .email(user.getEmail())
                .pwd(user.getPwd())
                .name(user.getName())
                .build();

        UserEntity createUser = userService.createUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUser.builder()
                        .userId(createUser.getId())
                        .email(createUser.getEmail())
                        .name(createUser.getName())
                        .createdAt(createUser.getCreatedAt())
                        .build()
        );
    }

    //비밀번호 확인
    @PostMapping("/users/confirm-password")
    public ResponseEntity<ResponseUser> confirmPassword(@RequestBody RequestUser user) {

        UserDto userDto = UserDto.builder()
                .email(user.getEmail())
                .pwd(user.getPwd())
                .name(user.getName())
                .build();

        UserEntity createUser = userService.createUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUser.builder()
                        .userId(createUser.getId())
                        .email(createUser.getEmail())
                        .name(createUser.getName())
                        .createdAt(createUser.getCreatedAt())
                        .build()
        );
    }

    //유저 정보 수정 (비밀번호, 닉네임, 전화번호)

    //로그아웃 (쿠키의 리프레시 토큰 제거)

    //전체 유저 조회
    @GetMapping("/users")
    public ResponseEntity<Page<ResponseUser>> getUsers(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UserDto> userPage = userService.getUserByAll(pageable);

        Page<ResponseUser> result = userPage.map(v -> ResponseUser.builder()
                .userId(v.getId())
                .email(v.getEmail())
                .name(v.getName())
                .createdAt(v.getCreatedAt()).build()
        );

        return ResponseEntity.ok(result);
    }

    //유저아이디로 유저 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity getUser(@PathVariable("userId") Long userId) {

        UserDto userDto = userService.getUserById(userId);

        List<ResponseAddress> requestAddressList = userDto.getAddresses().stream()
                .map(address -> ResponseAddress.builder()
                        .addressId(address.getAddressId())
                        .name(address.getName())
                        .address(address.getAddress())
                        .details(address.getDetails())
                        .defaultAddress(address.isDefaultAddress())
                        .build())
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userDto.getId())
                        .name(userDto.getName())
                        .email(userDto.getEmail())
                        .createdAt(userDto.getCreatedAt())
                        .addresses(requestAddressList)
                        .cache(userDto.getCache())
                        .point(userDto.getPoint())
                        .build()
        );
    }

    //배송지 추가
    @PostMapping("/users/{userId}/address")
    public ResponseEntity<ResponseUser> createAddress(@RequestBody RequestAddress address, @PathVariable("userId") Long userId) {

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

    //기본 배송지 수정
    @PatchMapping("/users/{userId}/address/{addressId}")
    public ResponseEntity<ResponseUser> editDefaultAddress(@PathVariable("userId") Long userId, @PathVariable("addressId") Long addressId) {

        UserEntity userEntity = userService.editDefaultAddress(userId, addressId);

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

    //배송지 삭제
    @DeleteMapping("/users/{userId}/address/{addressId}")
    public ResponseEntity<ResponseUser> deleteAddress(@PathVariable("userId") Long userId, @PathVariable("addressId") Long addressId) {

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
    @PatchMapping("/users/{userId}/cache/recharge/{amount}")
    public ResponseEntity<ResponseUser> rechargeCache(@PathVariable("userId") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.rechargeCache(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cache(userEntity.getCache())
                        .build()
        );
    }

    //캐시 차감
    @PatchMapping("/users/{userId}/cache/deduct/{amount}")
    public ResponseEntity<ResponseUser> deductCache(@PathVariable("userId") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductCache(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cache(userEntity.getCache())
                        .build()
        );
    }

    //포인트 충전
    @PatchMapping("/users/{userId}/point/recharge/{amount}")
    public ResponseEntity<ResponseUser> rechargePoint(@PathVariable("userId") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.rechargePoint(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }

    //포인트 차감
    @PatchMapping("/users/{userId}/point/deduct/{amount}")
    public ResponseEntity<ResponseUser> deductPoint(@PathVariable("userId") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductPoint(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }

    //쿠폰 추가
    //쿠폰 삭제
}
