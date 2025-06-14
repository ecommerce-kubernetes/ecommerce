package com.example.couponservice.controller;

import com.example.couponservice.dto.CouponDto;
import com.example.couponservice.jpa.entity.CouponEntity;
import com.example.couponservice.service.CouponService;
import com.example.couponservice.vo.RequestCreateCoupon;
import com.example.couponservice.vo.RequsetEditCoupon;
import com.example.couponservice.vo.ResponseCoupon;
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


@RestController
@Slf4j
@RequestMapping("/admin/coupon")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    //쿠폰 생성
    @PostMapping
    public ResponseEntity<ResponseCoupon> createCoupon(@Valid @RequestBody RequestCreateCoupon coupon) {

        CouponDto couponDto = CouponDto.builder()
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
                .build();

        CouponEntity couponEntity = couponService.createCoupon(couponDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseCoupon.builder()
                        .name(couponEntity.getName())
                        .description(couponEntity.getDescription())
                        .code(couponEntity.getCode())
                        .category(couponEntity.getCategory())
                        .discountType(couponEntity.getDiscountType())
                        .discountValue(couponEntity.getDiscountValue())
                        .minPurchaseAmount(couponEntity.getMinPurchaseAmount())
                        .maxDiscountAmount(couponEntity.getMaxDiscountAmount())
                        .validFrom(couponEntity.getValidFrom())
                        .validTo(couponEntity.getValidTo())
                        .reusable(couponEntity.isReusable())
                        .build()
        );
    }

    //쿠폰 목록 조회
    @GetMapping
    public ResponseEntity<Page<ResponseCoupon>> getCoupons(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<CouponDto> couponPage = couponService.getCouponByAll(pageable);

        Page<ResponseCoupon> result = couponPage.map(v -> ResponseCoupon.builder()
                .id(v.getId())
                .name(v.getName())
                .description(v.getDescription())
                .code(v.getCode())
                .category(v.getCategory())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minPurchaseAmount(v.getMinPurchaseAmount())
                .maxDiscountAmount(v.getMaxDiscountAmount())
                .validFrom(v.getValidFrom())
                .validTo(v.getValidTo())
                .reusable(v.isReusable())
                .build()
        );

        return ResponseEntity.ok(result);
    }

    //쿠폰 수정
    @PatchMapping
    public ResponseEntity<ResponseCoupon> updateCoupon(@Valid @RequestBody RequsetEditCoupon coupon) {

        CouponDto couponDto = CouponDto.builder()
                .id(coupon.getCouponId())
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
                .build();

        CouponEntity couponEntity = couponService.updateCoupon(couponDto);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //쿠폰 삭제
    @DeleteMapping("/{couponId}")
    public ResponseEntity<?> deleteUser(@PathVariable("couponId") Long couponId) {

        couponService.deleteCoupon(couponId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //유저 쿠폰 생성

    //유저 쿠폰 삭제

}
