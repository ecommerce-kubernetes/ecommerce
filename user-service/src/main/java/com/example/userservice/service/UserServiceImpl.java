package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    UserRepository userRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserEntity createUser(UserDto userDto) {

        UserEntity userEntity = UserEntity.builder()
                .email(userDto.getEmail())
                .encryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()))
                .name(userDto.getName())
                .build();

        return userRepository.save(userEntity);
    }

    @Override
    public List<UserDto> getUserByAll() {
        Iterable<UserEntity> userList = userRepository.findAll();
        List<UserDto> result = new ArrayList<>();
        userList.forEach(v -> {
            UserDto userDto = UserDto.builder()
                    .email(v.getEmail())
                    .name(v.getName())
                    .build();
            result.add(userDto);
        });

        return result;
    }

    @Override
    public UserEntity getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
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
                .pwd(userEntity.get().getEncryptedPwd())
                .name(userEntity.get().getName())
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
