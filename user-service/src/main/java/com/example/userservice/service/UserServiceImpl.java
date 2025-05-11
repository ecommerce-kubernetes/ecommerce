package com.example.userservice.service;

import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.AddressEntity;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService{

    UserRepository userRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    CircuitBreakerFactory circuitBreakerFactory;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, CircuitBreakerFactory circuitBreakerFactory) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public UserEntity createUser(UserDto userDto) {

        UserEntity userEntity = UserEntity.builder()
                .email(userDto.getEmail())
                .encryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()))
                .name(userDto.getName())
                .cache(0)
                .point(0)
                .build();

        try {
            return userRepository.save(userEntity);
        } catch (Exception e) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
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
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

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
                .createdAt(userEntity.getCreatedAt())
                .addresses(addressDtoList)
                .cache(userEntity.getCache())
                .point(userEntity.getPoint())
                .build();

    }

    @Override
    public UserDto getUserByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email"));

        return UserDto.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .build();
    }

    @Override
    public UserEntity addAddressByUserId(Long userId, AddressDto addressDto) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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
    public UserEntity editDefaultAddress(Long userId, Long addressId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.getAddresses().forEach(address -> {
            if (address.getId().equals(addressId)) {
                address.changeDefaultAddress(true);
            } else {
                address.changeDefaultAddress(false);
            }
        });

        return userRepository.save(user);
    }

    @Override
    public UserEntity deleteAddress(Long userId, Long addressId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        AddressEntity targetAddress = user.getAddresses().stream()
                .filter(address -> address.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        user.getAddresses().remove(targetAddress);

        return userRepository.save(user);
    }

    @Override
    public UserEntity rechargeCache(Long userId, int amount) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        user.rechargeCache(amount);

        return userRepository.save(user);
    }

    @Override
    public UserEntity deductCache(Long userId, int amount) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }

        if (user.getCache() - amount < 0) {
            throw new IllegalArgumentException("금액이 모자릅니다.");
        }

        user.deductCache(amount);

        return userRepository.save(user);
    }

    @Override
    public UserEntity rechargePoint(Long userId, int amount) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        user.rechargePoint(amount);

        return userRepository.save(user);
    }

    @Override
    public UserEntity deductPoint(Long userId, int amount) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }

        if (user.getPoint() - amount < 0) {
            throw new IllegalArgumentException("금액이 모자릅니다.");
        }

        user.deductPoint(amount);

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userEntity = userRepository.findByEmail(username);
        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username");
        }

        return new User(
                userEntity.get().getEmail(),
                userEntity.get().getEncryptedPwd(),
                true,
                true,
                true,
                true,
                new ArrayList<>()
        );
    }
}
