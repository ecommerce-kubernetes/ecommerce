package com.example.couponservice.controller;

import com.example.common.DiscountType;
import com.example.couponservice.dto.CouponDto;
import com.example.couponservice.jpa.entity.CouponEntity;
import com.example.couponservice.jpa.entity.UserCouponEntity;
import com.example.couponservice.service.CouponService;
import com.example.couponservice.vo.ResponseCoupon;
import com.example.couponservice.vo.ResponseUserCoupon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/coupon")
public class UserCouponController {

    private final CouponService couponService;

    public UserCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    //유저 쿠폰 발급
    @PostMapping("/{couponCode}")
    public ResponseEntity<ResponseCoupon> IssuedCoupon(@RequestHeader("X-User-Id") Long userId, @PathVariable("couponCode") String couponCode) {

        couponService.issuedCouponByUser(userId, couponCode);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //유저 쿠폰 핸드폰 번호 업데이트
    @PutMapping("/change/phone-number")
    public ResponseEntity<?> changePhoneNumber(@RequestHeader("X-User-Id") Long userId,
                                               @RequestParam("phoneNumber") String phoneNumber) {

        couponService.changePhoneNumber(userId, phoneNumber);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //유저 사용가능한 쿠폰(유효기간이 지나지 않은 것들 중 사용하지 않은 쿠폰) 조회
    @GetMapping("/available")
    public ResponseEntity<List<ResponseUserCoupon>> getAllValidCoupon(@RequestHeader("X-User-Id") Long userId) {

        List<UserCouponEntity> couponList = couponService.getAllValidCouponByUser(userId);

        List<ResponseUserCoupon> responseList = couponList.stream()
                .map(userCoupon -> ResponseUserCoupon.builder()
                        .id(userCoupon.getId())
                        .responseCoupon(ResponseCoupon.builder()
                                .id(userCoupon.getCoupon().getId())
                                .name(userCoupon.getCoupon().getName())
                                .description(userCoupon.getCoupon().getDescription())
                                .code(userCoupon.getCoupon().getCode())
                                .category(userCoupon.getCoupon().getCategory())
                                .discountType(userCoupon.getCoupon().getDiscountType())
                                .discountValue(userCoupon.getCoupon().getDiscountValue())
                                .minPurchaseAmount(userCoupon.getCoupon().getMinPurchaseAmount())
                                .maxDiscountAmount(userCoupon.getCoupon().getMaxDiscountAmount())
                                .validFrom(userCoupon.getCoupon().getValidFrom())
                                .validTo(userCoupon.getCoupon().getValidTo())
                                .reusable(userCoupon.getCoupon().isReusable())
                                .build())
                        .used(userCoupon.isUsed())
                        .issuedAt(userCoupon.getIssuedAt())
                        .usedAt(userCoupon.getUsedAt())
                        .expiresAt(userCoupon.getExpiresAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    //유저 사용가능하지 않은 쿠폰(유효기간이 지났거나 사용한 쿠폰) 조회
    @GetMapping("/expired-or-used")
    public ResponseEntity<List<ResponseUserCoupon>> getAllExpiredOrUsedCoupon(@RequestHeader("X-User-Id") Long userId) {

        List<UserCouponEntity> couponList = couponService.getAllExpiredOrUsedCouponByUser(userId);

        List<ResponseUserCoupon> responseList = couponList.stream()
                .map(userCoupon -> ResponseUserCoupon.builder()
                        .id(userCoupon.getId())
                        .responseCoupon(ResponseCoupon.builder()
                                .id(userCoupon.getCoupon().getId())
                                .name(userCoupon.getCoupon().getName())
                                .description(userCoupon.getCoupon().getDescription())
                                .code(userCoupon.getCoupon().getCode())
                                .category(userCoupon.getCoupon().getCategory())
                                .discountType(userCoupon.getCoupon().getDiscountType())
                                .discountValue(userCoupon.getCoupon().getDiscountValue())
                                .minPurchaseAmount(userCoupon.getCoupon().getMinPurchaseAmount())
                                .maxDiscountAmount(userCoupon.getCoupon().getMaxDiscountAmount())
                                .validFrom(userCoupon.getCoupon().getValidFrom())
                                .validTo(userCoupon.getCoupon().getValidTo())
                                .reusable(userCoupon.getCoupon().isReusable())
                                .build())
                        .used(userCoupon.isUsed())
                        .issuedAt(userCoupon.getIssuedAt())
                        .usedAt(userCoupon.getUsedAt())
                        .expiresAt(userCoupon.getExpiresAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }


    @GetMapping("/available/{userId}/{userCouponId}")
    public ResponseEntity<ResponseCoupon> useCoupon(@PathVariable("userId") Long userId, @PathVariable("userCouponId") Long couponId) {

        CouponDto couponDto = couponService.availableUserCoupon(userId, couponId);

        return ResponseEntity.ok(
                ResponseCoupon.builder()
                .discountType(couponDto.getDiscountType())
                .discountValue(couponDto.getDiscountValue())
                .minPurchaseAmount(couponDto.getMinPurchaseAmount())
                .maxDiscountAmount(couponDto.getMaxDiscountAmount())
                .build()
        );
    }

    //유저 쿠폰 카테고리별 조회 & 최소금액

    //유저 쿠폰 사용
//    @PostMapping("/use/{couponId}")
//    public ResponseEntity<ResponseCoupon> useCoupon(@RequestHeader("X-User-Id") Long userId, @PathVariable("couponId") Long couponId) {
//
//        couponService.useCouponByUser(userId, couponId, String category);
//
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }

}
