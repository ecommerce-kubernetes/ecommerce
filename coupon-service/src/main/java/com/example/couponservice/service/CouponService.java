package com.example.couponservice.service;

import com.example.couponservice.dto.CouponDto;
import com.example.couponservice.jpa.entity.CouponEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponService {

    CouponEntity createCoupon(CouponDto couponDto);

    Page<CouponDto> getCouponByAll(Pageable pageable);

    CouponEntity updateCoupon(CouponDto couponDto);

    void deleteCoupon(Long couponId);

    void issuedCouponByUser(Long userId, String couponCode);

    List<CouponEntity> getAllValidCouponByUser(Long userId);

    List<CouponEntity> getAllExpiredOrUsedCouponByUser(Long userId);

    void useCouponByUser(Long userId, Long couponId);

    void changePhoneNumber(Long userId, String phoneNumber);
}
