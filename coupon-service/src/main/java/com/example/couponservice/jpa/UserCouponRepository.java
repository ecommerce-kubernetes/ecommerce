package com.example.couponservice.jpa;

import com.example.couponservice.jpa.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long> {

    List<UserCouponEntity> findAllByUserId(Long userId);

    Optional<UserCouponEntity> findByCouponIdAndUserId(Long couponId, Long userId);

    boolean existsByUserIdOrPhoneNumberAndCoupon_Code(Long userId, String phoneNumber, String couponCode);
}
