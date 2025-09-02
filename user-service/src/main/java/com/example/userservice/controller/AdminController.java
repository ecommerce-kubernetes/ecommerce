package com.example.userservice.controller;

import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.entity.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestCreateAddress;
import com.example.userservice.vo.RequestEditUser;
import com.example.userservice.vo.ResponseAddress;
import com.example.userservice.vo.ResponseUser;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/V1__create_users_table.sql")
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    //전체 유저 조회
    @GetMapping
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
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") Long userId) {

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
                        .email(userDto.getEmail())
                        .name(userDto.getName())
                        .gender(userDto.getGender())
                        .birthDate(userDto.getBirthDate())
                        .phoneNumber(userDto.getPhoneNumber())
                        .phoneVerified(userDto.isPhoneVerified())
                        .createdAt(userDto.getCreatedAt())
                        .addresses(requestAddressList)
                        .cache(userDto.getCache())
                        .point(userDto.getPoint())
                        .build()
        );
    }

    //유저 정보 수정
    @PatchMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @Valid @RequestBody RequestEditUser user) {

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

    //유저 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId) {

        userService.deleteUser(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //배송지 추가
    @PostMapping("/{userId}/address")
    public ResponseEntity<ResponseUser> createAddress(@PathVariable("userId") Long userId, @Valid @RequestBody RequestCreateAddress address) {

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
    @PatchMapping("/{userId}/address")
    public ResponseEntity<ResponseUser> updateAddress(@PathVariable("userId") Long userId, @Valid @RequestBody RequestCreateAddress address) {

        AddressDto addressDto = AddressDto.builder()
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
    @DeleteMapping("/{userId}/address/{addressId}")
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
    @PatchMapping("/{userId}/cache/recharge/{amount}")
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
    @PatchMapping("/{userId}/cache/deduct/{amount}")
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
    @PatchMapping("/{userId}/point/recharge/{amount}")
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
    @PatchMapping("/{userId}/point/deduct/{amount}")
    public ResponseEntity<ResponseUser> deductPoint(@PathVariable("userId") Long userId, @PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductPoint(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }
}
