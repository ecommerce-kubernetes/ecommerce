package com.example.couponservice.jpa;

import com.example.couponservice.jpa.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long> {

    List<UserCouponEntity> findAllByUserId(Long userId);

    List<UserCouponEntity> findAllByUserIdAndUsedFalseAndExpiresAtAfter(Long userId, LocalDateTime now);

    List<UserCouponEntity> findAllByUserIdAndUsedTrueOrUserIdAndExpiresAtBefore(Long userId1, Long userId2, LocalDateTime now);

    boolean existsByCouponIdAndUserIdOrCouponIdAndPhoneNumber(Long couponId1, Long userId, Long couponId2, String phoneNumber);

    Optional<UserCouponEntity> findByIdAndUserId(Long userCouponId, Long userId);
}
