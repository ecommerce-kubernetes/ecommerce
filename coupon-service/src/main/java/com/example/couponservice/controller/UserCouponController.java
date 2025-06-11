package com.example.couponservice.controller;

import com.example.couponservice.jpa.entity.CouponEntity;
import com.example.couponservice.service.CouponService;
import com.example.couponservice.vo.ResponseCoupon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<ResponseCoupon>> getAllValidCoupon(@RequestHeader("X-User-Id") Long userId) {

        List<CouponEntity> couponList = couponService.getAllValidCouponByUser(userId);

        List<ResponseCoupon> responseList = couponList.stream()
                .map(coupon -> ResponseCoupon.builder()
                        .id(coupon.getId())
                        .name(coupon.getName())
                        .description(coupon.getDescription())
                        .category(coupon.getCategory())
                        .discountType(coupon.getDiscountType())
                        .discountValue(coupon.getDiscountValue())
                        .minPurchaseAmount(coupon.getMinPurchaseAmount())
                        .maxDiscountAmount(coupon.getMaxDiscountAmount())
                        .validFrom(coupon.getValidFrom())
                        .validTo(coupon.getValidTo())
                        .reusable(coupon.isReusable())
                        .build()
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    //유저 사용가능하지 않은 쿠폰(유효기간이 지났거나 사용한 쿠폰) 조회
    @GetMapping("/expired-or-used")
    public ResponseEntity<List<ResponseCoupon>> getAllExpiredOrUsedCoupon(@RequestHeader("X-User-Id") Long userId) {

        List<CouponEntity> couponList = couponService.getAllExpiredOrUsedCouponByUser(userId);

        List<ResponseCoupon> responseList = couponList.stream()
                .map(coupon -> ResponseCoupon.builder()
                        .id(coupon.getId())
                        .name(coupon.getName())
                        .description(coupon.getDescription())
                        .category(coupon.getCategory())
                        .discountType(coupon.getDiscountType())
                        .discountValue(coupon.getDiscountValue())
                        .minPurchaseAmount(coupon.getMinPurchaseAmount())
                        .maxDiscountAmount(coupon.getMaxDiscountAmount())
                        .validFrom(coupon.getValidFrom())
                        .validTo(coupon.getValidTo())
                        .reusable(coupon.isReusable())
                        .build()
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    //유저 쿠폰 사용
    @PostMapping("/use/{couponId}")
    public ResponseEntity<ResponseCoupon> useCoupon(@RequestHeader("X-User-Id") Long userId, @PathVariable("couponId") Long couponId) {

        couponService.useCouponByUser(userId, couponId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
