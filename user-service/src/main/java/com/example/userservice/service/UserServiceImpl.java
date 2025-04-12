package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
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
                .createAt(v.getCreateAt())
                .build());
    }

    @Override
    public UserDto getUserById(Long userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // Address 객체를 List<String> 형태로 변환
        List<String> userAddresses = userEntity.getAddresses().stream()
                .map(addr -> addr.getAddress().getAddressAll())
                .collect(Collectors.toList());

        return UserDto.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .pwd(userEntity.getEncryptedPwd())
                .name(userEntity.getName())
                .createAt(userEntity.getCreateAt())
                .addresses(userAddresses)
                .build();

    }

    @Override
    public UserDto getUserByEmail(String email) {
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email");
        }

        return UserDto.builder()
                .id(userEntity.get().getId())
                .email(userEntity.get().getEmail())
                .build();
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
