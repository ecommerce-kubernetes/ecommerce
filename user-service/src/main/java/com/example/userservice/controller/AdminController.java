package com.example.userservice.controller;

import com.example.userservice.config.specification.annotation.BadRequestApiResponse;
import com.example.userservice.config.specification.annotation.ConflictApiResponse;
import com.example.userservice.config.specification.annotation.ForbiddenApiResponse;
import com.example.userservice.config.specification.annotation.NotFoundApiResponse;
import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.entity.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestCreateAddress;
import com.example.userservice.vo.RequestEditUser;
import com.example.userservice.vo.ResponseAddress;
import com.example.userservice.vo.ResponseUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/admin/users")
@Slf4j
@Tag(name = "Admin-User", description = "유저 관련 관리자 API")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    //전체 유저 조회
    @GetMapping
    @Operation(summary = "전체 유저 조회", description = "전체 유저를 조회합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
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
    @Operation(summary = "유저 조회", description = "userId로 유저를 조회합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> getUser(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId) {

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
                        .cash(userDto.getCash())
                        .point(userDto.getPoint())
                        .build()
        );
    }

    //유저 정보 수정
    @PatchMapping("/{userId}")
    @Operation(summary = "유저 정보 수정", description = "userId에 해당하는 유저 정보를 수정합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<?> updateUser(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId, @Valid @RequestBody RequestEditUser user) {

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
    @Operation(summary = "유저 삭제", description = "userId에 해당하는 유저를 삭제합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<?> deleteUser(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId) {

        userService.deleteUser(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //배송지 추가
    @PostMapping("/{userId}/address")
    @Operation(summary = "유저 배송지 등록", description = "userId에 해당하는 유저의 배송지를 등록합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    public ResponseEntity<ResponseUser> createAddress(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId, @Valid @RequestBody RequestCreateAddress address) {

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
    @Operation(summary = "유저 배송지 정보 수정", description = "userId에 해당하는 유저의 배송지 정보를 수정합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> updateAddress(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId, @Valid @RequestBody RequestCreateAddress address) {

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
    @Operation(summary = "유저 배송지 삭제", description = "userId에 해당하는 유저의 배송지를 삭제합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> deleteAddress(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId, @Parameter(description = "주소 ID")@PathVariable("addressId") Long addressId) {

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
    @PatchMapping("/{userId}/cash/recharge/{amount}")
    @Operation(summary = "유저 캐시 충전", description = "userId에 해당하는 유저의 캐쉬를 충전합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> rechargeCash(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId, @Parameter(description = "금액")@PathVariable("amount") int amount) {

        UserEntity userEntity = userService.rechargeCash(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cash(userEntity.getCash())
                        .build()
        );
    }

    //캐시 차감
    @PatchMapping("/{userId}/cash/deduct/{amount}")
    @Operation(summary = "유저 캐시 차감", description = "userId에 해당하는 유저의 캐쉬를 차감합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> deductCash(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId, @Parameter(description = "금액")@PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductCash(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .cash(userEntity.getCash())
                        .build()
        );
    }

    //포인트 충전
    @PatchMapping("/{userId}/point/recharge/{amount}")
    @Operation(summary = "유저 포인트 충전", description = "userId에 해당하는 유저의 포인트를 충전합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> rechargePoint(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId, @Parameter(description = "포인트")@PathVariable("amount") int amount) {

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
    @Operation(summary = "유저 포인트 차감", description = "userId에 해당하는 유저의 포인트를 차감합니다.")
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    public ResponseEntity<ResponseUser> deductPoint(@Parameter(description = "유저 ID")@PathVariable("userId") Long userId, @Parameter(description = "포인트")@PathVariable("amount") int amount) {

        UserEntity userEntity = userService.deductPoint(userId, amount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUser.builder()
                        .userId(userEntity.getId())
                        .point(userEntity.getPoint())
                        .build()
        );
    }
}
