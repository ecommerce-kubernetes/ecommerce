package com.example.couponservice.service;

import com.example.couponservice.advice.exceptions.*;
import com.example.couponservice.client.UserServiceClient;
import com.example.couponservice.dto.CouponDto;
import com.example.couponservice.jpa.CouponRepository;
import com.example.couponservice.jpa.UserCouponRepository;
import com.example.couponservice.jpa.entity.CouponEntity;
import com.example.couponservice.jpa.entity.UserCouponEntity;
import com.example.couponservice.vo.ResponseUser;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class CouponServiceImpl implements CouponService{

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserServiceClient userServiceClient;

    public CouponServiceImpl(CouponRepository couponRepository, UserCouponRepository userCouponRepository, UserServiceClient userServiceClient) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public CouponEntity createCoupon(CouponDto couponDto) {

        // 쿠폰 이름 중복 확인
        if (couponRepository.existsByName(couponDto.getName())) {
            throw new IsExistCouponException("이미 존재하는 쿠폰이름입니다.");
        }

        // 쿠폰 코드 생성 및 중복 확인
        String couponCode = generateCouponCode();

        if (couponRepository.existsByCode(couponCode)) {
            throw new IsExistCouponException("이미 존재하는 쿠폰코드입니다.");
        }


        // CouponDto → CouponEntity 변환
        CouponEntity coupon = CouponEntity.builder()
                .name(couponDto.getName())
                .description(couponDto.getDescription())
                .code(couponCode)
                .category(couponDto.getCategory())
                .discountType(couponDto.getDiscountType())
                .discountValue(couponDto.getDiscountValue())
                .minPurchaseAmount(couponDto.getMinPurchaseAmount())
                .maxDiscountAmount(couponDto.getMaxDiscountAmount())
                .validFrom(couponDto.getValidFrom())
                .validTo(couponDto.getValidTo())
                .reusable(couponDto.isReusable())
                .build();

        // 저장
        return couponRepository.save(coupon);
    }

    @Override
    public Page<CouponDto> getCouponByAll(Pageable pageable) {

        Page<CouponEntity> couponList = couponRepository.findAll(pageable);

        return couponList.map(v -> CouponDto.builder()
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
                .build());
    }

    @Override
    public CouponEntity updateCoupon(CouponDto couponDto) {
        CouponEntity couponEntity = couponRepository.findById(couponDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 쿠폰을 찾을 수 없습니다: " + couponDto.getId()));

        if (couponDto.getName() != null && !couponDto.getName().isEmpty()) {
            couponEntity.changeName(couponDto.getName());
        }

        if (couponDto.getCategory() != null && !couponDto.getCategory().isEmpty()) {
            couponEntity.changeCategory(couponDto.getCategory());
        }

        if (couponDto.getDescription() != null) {
            couponEntity.changeDescription(couponDto.getDescription());
        }

        if (couponDto.getDiscountType() != null) {
            couponEntity.changeDiscountType(couponDto.getDiscountType());
        }

        if (!(couponDto.getDiscountValue() < 0)) {
            couponEntity.changeDiscountValue(couponDto.getDiscountValue());
        }

        if (!(couponDto.getMinPurchaseAmount() < 0)) {
            couponEntity.changeMinPurchaseAmount(couponDto.getMinPurchaseAmount());
        }

        if (!(couponDto.getMinPurchaseAmount() < 0)) {
            couponEntity.changeMaxDiscountAmount(couponDto.getMaxDiscountAmount());
        }

        if (couponDto.getValidFrom() != null) {
            couponEntity.changeValidFrom(couponDto.getValidFrom());
        }

        if (couponDto.getValidTo() != null) {
            couponEntity.changeValidTo(couponDto.getValidTo());
        }

        couponEntity.changeReusable(couponDto.isReusable());

        return couponRepository.save(couponEntity);
    }

    @Override
    public void deleteCoupon(Long couponId) {
        try {
            couponRepository.deleteById(couponId);
        } catch (EmptyResultDataAccessException ex) {
            throw new EntityNotFoundException("존재하지 않는 쿠폰입니다. ID: " + couponId);
        }
    }

    @Override
    public void issuedCouponByUser(Long userId, String couponCode) {

        ResponseUser responseUser = userServiceClient.getMyUserData(userId);

        //핸드폰 번호가 존재하는가 & 핸드폰 번호 인증이 되었는가
        if (responseUser.getPhoneNumber() == null || !responseUser.isPhoneVerified()) {
            throw new InvalidPhoneNumberException("핸드폰 번호 인증이 안되었습니다.");
        }

        //이미 발급받은 쿠폰인가
        CouponEntity couponEntity = couponRepository.findByCode(couponCode);
        if (couponEntity == null) {
            throw new IsExistCouponException("해당 코드의 쿠폰이 존재하지 않습니다.");
        }

        //재발급 가능하지 않은 쿠폰일 때, 해당 쿠폰 코드로 발급받은 대상이 userId 나 핸드폰 번호일 경우
        if (!couponEntity.isReusable()) {
            if (userCouponRepository.existsByCouponIdAndUserIdOrCouponIdAndPhoneNumber(couponEntity.getId(), responseUser.getUserId(), couponEntity.getId(), responseUser.getPhoneNumber()))
            {
                throw new AlreadyUsedCouponException("이미 발급된 쿠폰입니다: " + couponCode);
            }
        }

        //쿠폰 발급
        UserCouponEntity userCouponEntity = UserCouponEntity.builder()
                .coupon(couponEntity)
                .userId(responseUser.getUserId())
                .phoneNumber(responseUser.getPhoneNumber())
                .used(false)
                .usedAt(null)
                .expiresAt(couponEntity.getValidTo())
                .build();

        userCouponRepository.save(userCouponEntity);

    }

    @Override
    public List<UserCouponEntity> getAllValidCouponByUser(Long userId) {

        return userCouponRepository.findAllByUserIdAndUsedFalseAndExpiresAtAfter(userId, LocalDateTime.now());
    }

    @Override
    public List<UserCouponEntity> getAllExpiredOrUsedCouponByUser(Long userId) {

        return userCouponRepository.findAllByUserIdAndUsedTrueOrUserIdAndExpiresAtBefore(userId, userId, LocalDateTime.now());
    }

    @Override
    public CouponDto useCouponByUser(Long userCouponId) {

        // 유저가 발급받은 해당 쿠폰 조회
        UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IsExistCouponException("해당 쿠폰이 존재하지 않거나 사용자에게 속하지 않습니다."));

        //만료기간이 지났는지 확인
        if (LocalDateTime.now().isAfter(userCoupon.getExpiresAt())) {
            throw new IsExpiredCouponException("해당 쿠폰의 만료기간이 지났습니다.");
        }

        // used, usedAt 필드 업데이트, 이미 사용한 쿠폰일 시 에러
        userCoupon.markAsUsed(LocalDateTime.now());

        //쿠폰 정보
        CouponEntity couponEntity = userCoupon.getCoupon();

        return CouponDto.builder()
                .id(couponEntity.getId())
                .name(couponEntity.getName())
                .code(couponEntity.getCode())
                .description(couponEntity.getDescription())
                .category(couponEntity.getCategory())
                .discountType(couponEntity.getDiscountType())
                .discountValue(couponEntity.getDiscountValue())
                .minPurchaseAmount(couponEntity.getMinPurchaseAmount())
                .maxDiscountAmount(couponEntity.getMaxDiscountAmount())
                .validFrom(couponEntity.getValidFrom())
                .validTo(couponEntity.getValidTo())
                .reusable(couponEntity.isReusable())
                .build();
    }

    @Override
    public CouponDto availableUserCoupon(Long userId, Long userCouponId) {

        // 유저가 발급받은 해당 쿠폰 조회
        UserCouponEntity userCoupon = userCouponRepository.findByIdAndUserId(userCouponId, userId)
                .orElseThrow(() -> new IsExistCouponException("해당 쿠폰이 존재하지 않거나 사용자에게 속하지 않습니다."));

        //만료기간이 지났는지 확인
        if (LocalDateTime.now().isAfter(userCoupon.getExpiresAt())) {
            throw new IsExpiredCouponException("해당 쿠폰의 만료기간이 지났습니다.");
        }

        //이미 사용한 쿠폰일 시 에러
        if (userCoupon.isUsed()) {
            throw new AlreadyUsedCouponException("이미 사용한 쿠폰입니다.");
        }

        //쿠폰 정보
        CouponEntity couponEntity = userCoupon.getCoupon();

        return CouponDto.builder()
                .id(couponEntity.getId())
                .name(couponEntity.getName())
                .code(couponEntity.getCode())
                .description(couponEntity.getDescription())
                .category(couponEntity.getCategory())
                .discountType(couponEntity.getDiscountType())
                .discountValue(couponEntity.getDiscountValue())
                .minPurchaseAmount(couponEntity.getMinPurchaseAmount())
                .maxDiscountAmount(couponEntity.getMaxDiscountAmount())
                .validFrom(couponEntity.getValidFrom())
                .validTo(couponEntity.getValidTo())
                .reusable(couponEntity.isReusable())
                .build();
    }

    @Override
    public void revertUserCoupon(Long userCouponId) {
        // 유저가 발급받은 해당 쿠폰 조회
        UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IsExistCouponException("해당 쿠폰이 존재하지 않거나 사용자에게 속하지 않습니다."));

        userCoupon.revertAsUsed();
    }

    @Override
    public void changePhoneNumber(Long userId, String phoneNumber) {
        List<UserCouponEntity> userCouponEntityList = userCouponRepository.findAllByUserId(userId);

        for (UserCouponEntity userCoupon : userCouponEntityList) {
            userCoupon.changePhoneNumber(phoneNumber);
        }
    }

    private String generateCouponCode() {
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                codeBuilder.append("-");
            }
            for (int j = 0; j < 4; j++) {
                char letter = (char) ('A' + random.nextInt(26));
                codeBuilder.append(letter);
            }
        }

        return codeBuilder.toString();
    }
}
