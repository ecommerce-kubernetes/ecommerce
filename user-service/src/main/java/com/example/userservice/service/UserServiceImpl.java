package com.example.userservice.service;

import com.example.userservice.advice.exceptions.InvalidAmountException;
import com.example.userservice.advice.exceptions.InvalidPasswordException;
import com.example.userservice.advice.exceptions.UserNotFoundException;
import com.example.userservice.client.CouponServiceClient;
import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.*;
import com.example.userservice.jpa.entity.AddressEntity;
import com.example.userservice.jpa.entity.Gender;
import com.example.userservice.jpa.entity.Role;
import com.example.userservice.jpa.entity.UserEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final CouponServiceClient couponServiceClient;

    public UserServiceImpl(CouponServiceClient couponServiceClient, CircuitBreakerFactory circuitBreakerFactory, BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {
        this.couponServiceClient = couponServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity createUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        UserEntity userEntity = UserEntity.builder()
                .email(userDto.getEmail())
                .encryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()))
                .name(userDto.getName())
                .gender(Gender.valueOf(userDto.getGender()))
                .birthDate(LocalDate.parse(userDto.getBirthDate()))
                .phoneNumber(userDto.getPhoneNumber())
                .phoneVerified(userDto.isPhoneVerified())
                .cache(0)
                .point(0)
                .role(Role.ROLE_USER)
                .build();

        try {
            return userRepository.save(userEntity);
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            throw new RuntimeException("회원가입에 실패했습니다. 관리자에게 문의하세요.");
        }
    }

    @Override
    public Page<UserDto> getUserByAll(Pageable pageable) {
        Page<UserEntity> userList = userRepository.findAll(pageable);

        return userList.map(v -> UserDto.builder()
                .id(v.getId())
                .email(v.getEmail())
                .name(v.getName())
                .createdAt(v.getCreatedAt())
                .build());
    }

    @Override
    public UserDto getUserById(Long userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        List<AddressDto> addressDtoList = userEntity.getAddresses().stream()
                .map(address -> AddressDto.builder()
                        .addressId(address.getId())
                        .name(address.getName())
                        .address(address.getAddress())
                        .details(address.getDetails())
                        .defaultAddress(address.isDefaultAddress())
                        .build())
                .collect(Collectors.toList());

        return UserDto.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .pwd(userEntity.getEncryptedPwd())
                .name(userEntity.getName())
                .phoneNumber(userEntity.getPhoneNumber())
                .phoneVerified(userEntity.isPhoneVerified())
                .gender(String.valueOf(userEntity.getGender()))
                .birthDate(String.valueOf(userEntity.getBirthDate()))
                .cache(userEntity.getCache())
                .point(userEntity.getPoint())
                .createdAt(userEntity.getCreatedAt())
                .addresses(addressDtoList)
                .build();

    }

    @Override
    public UserDto getUserByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));

        return UserDto.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .build();
    }

    @Override
    public UserEntity updateUser(UserDto userDto) {
        UserEntity userEntity = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userDto.getId()));

        if (userDto.getName() != null && !userDto.getName().isEmpty()) {
            userEntity.changeName(userDto.getName());
        }

        if (userDto.getPwd() != null && !userDto.getPwd().isEmpty()) {
            String encryptedPwd = bCryptPasswordEncoder.encode(userDto.getPwd());
            userEntity.changePassword(encryptedPwd);
        }

        if (userDto.getPhoneNumber() != null && !userDto.getPhoneNumber().isEmpty()) {
            userEntity.changePhoneNumber(userDto.getPhoneNumber());
            userEntity.changeIsPhoneVerified(true);
            //쿠폰 필드 업데이트
            couponServiceClient.changePhoneNumber(userEntity.getId(), userDto.getPhoneNumber());
        }

        if (userDto.getGender() != null && !userDto.getGender().isEmpty()) {
            userEntity.changeGender(userDto.getGender());
        }

        if (userDto.getBirthDate() != null && !userDto.getBirthDate().isEmpty()) {
            userEntity.changeBirthDate(userDto.getBirthDate());
        }

        return userRepository.save(userEntity);
    }

    @Override
    public void verifyPhoneNumber(Long userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        userEntity.changeIsPhoneVerified(true);
    }

    @Override
    public void checkUser(String email, String password) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));

        if (!bCryptPasswordEncoder.matches(password, userEntity.getEncryptedPwd())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    @Override
    public List<AddressEntity> getAddressesByUserId(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        return user.getAddresses();
    }

    @Override
    public UserEntity addAddressByUserId(Long userId, AddressDto addressDto) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        // 1. 기본 주소 설정 요청 시 기존 기본 주소 false로 초기화
        if (addressDto.isDefaultAddress()) {
            user.getAddresses().stream()
                    .filter(AddressEntity::isDefaultAddress)
                    .forEach(address -> address.changeDefaultAddress(false));
        }

        // 2. 새 주소 생성
        AddressEntity newAddress = AddressEntity.builder()
                .user(user)
                .name(addressDto.getName())
                .address(addressDto.getAddress())
                .details(addressDto.getDetails())
                .defaultAddress(addressDto.isDefaultAddress())
                .build();

        // 3. 유저에 추가
        user.getAddresses().add(newAddress);

        // 4. 저장 (Cascade로 address 자동 저장)
        return userRepository.save(user);
    }

    @Override
    public UserEntity updateAddress(Long userId, AddressDto addressDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        for (AddressEntity address : user.getAddresses()) {
            boolean isTarget = address.getName().equals(addressDto.getName());

            if (isTarget) {
                updateAddressFields(address, addressDto);
                if (addressDto.isDefaultAddress()) {
                    address.changeDefaultAddress(true);
                }
            } else {
                if (addressDto.isDefaultAddress()) {
                    address.changeDefaultAddress(false);
                }
            }
        }

        return userRepository.save(user);
    }

    private void updateAddressFields(AddressEntity address, AddressDto dto) {
        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            address.changeAddress(dto.getAddress());
        }
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            address.changeDetails(dto.getDetails());
        }
    }

    @Override
    public UserEntity deleteAddress(Long userId, String addressName) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        AddressEntity targetAddress = user.getAddresses().stream()
                .filter(address -> address.getName().equals(addressName))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("해당 주소를 찾을 수 없습니다: " + addressName));

        user.getAddresses().remove(targetAddress);

        return userRepository.save(user);
    }

    @Override
    public UserEntity rechargeCache(Long userId, int amount) {
        if (amount <= 0) throw new InvalidAmountException("충전 금액은 0보다 커야 합니다.");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        user.rechargeCache(amount);
        return userRepository.save(user);
    }

    @Override
    public UserEntity deductCache(Long userId, int amount) {
        if (amount <= 0) throw new InvalidAmountException("차감 금액은 0보다 커야 합니다.");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        if (user.getCache() < amount) throw new InvalidAmountException("캐시가 부족합니다.");

        user.deductCache(amount);
        return userRepository.save(user);
    }

    @Override
    public UserEntity rechargePoint(Long userId, int amount) {
        if (amount <= 0) throw new InvalidAmountException("충전 금액은 0보다 커야 합니다.");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        user.rechargePoint(amount);
        return userRepository.save(user);
    }

    @Override
    public UserEntity deductPoint(Long userId, int amount) {
        if (amount <= 0) throw new InvalidAmountException("차감 금액은 0보다 커야 합니다.");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        if (user.getPoint() < amount) throw new InvalidAmountException("포인트가 부족합니다.");

        user.deductPoint(amount);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException ex) {
            throw new EntityNotFoundException("존재하지 않는 사용자입니다. ID: " + userId);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws BadCredentialsException {
        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 잘못되었습니다."));

        GrantedAuthority authority = new SimpleGrantedAuthority(userEntity.getRole().name());

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(), List.of(authority));
    }
}
