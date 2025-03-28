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
    public UserDto createUser(UserDto userDto) {

        UserEntity userEntity = new UserEntity(
                userDto.getEmail(),
                userDto.getName(),
                bCryptPasswordEncoder.encode(userDto.getPwd())
        );

        UserEntity saveUserEntity = userRepository.save(userEntity);

        UserDto result = new UserDto();
        result.setId(saveUserEntity.getId());
        result.setEmail(saveUserEntity.getEmail());
        result.setName(saveUserEntity.getName());
        return result;

    }

    @Override
    public List<UserDto> getUserByAll() {
        Iterable<UserEntity> userList = userRepository.findAll();
        List<UserDto> result = new ArrayList<>();
        userList.forEach(v -> {
            UserDto userDto = new UserDto();
            userDto.setEmail(v.getEmail());
            userDto.setName(v.getName());
            result.add(userDto);
        });

        return result;
    }

    @Override
    public Optional<UserEntity> getUserById(Long userId) {

        return userRepository.findById(userId);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }

        UserDto userDto = new UserDto();
        userDto.setId(userEntity.get().getId());
        userDto.setEmail(userEntity.get().getEmail());
        userDto.setPwd(userEntity.get().getEncryptedPwd());
        userDto.setName(userEntity.get().getName());
        return userDto;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userEntity = userRepository.findByEmail(username);
        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException(username);
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
