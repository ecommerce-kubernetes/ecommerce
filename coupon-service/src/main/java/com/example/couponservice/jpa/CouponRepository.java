package com.example.couponservice.jpa;

import com.example.couponservice.jpa.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;



public interface CouponRepository extends JpaRepository<CouponEntity, Long> {

    boolean existsByName(String name);

    boolean existsByCode(String couponCode);

    CouponEntity findByCode(String couponCode);
}
