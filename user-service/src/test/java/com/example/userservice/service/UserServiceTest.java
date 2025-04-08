package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
@ExtendWith(MockitoExtension.class)
@Slf4j
class UserServiceTest {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();  // 테스트 실행 전에 기존 데이터 삭제
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();  // 테스트 실행 후 데이터 삭제
    }

    @Test
    void createUser_성공() {
        // Given
        UserDto userDto = UserDto.builder()
                .email("test@email.com")
                .pwd("password")
                .name("Test User")
                .build();

        // When
        UserEntity result = userService.createUser(userDto);

        // Then
        assertNotNull(result);
        assertTrue(bCryptPasswordEncoder.matches(userDto.getPwd(), result.getEncryptedPwd()));
        assertEquals("test@email.com", result.getEmail());
    }

    @Test
    void getUserByAll_성공() {
        // Given
        UserDto userDto1 = UserDto.builder()
                .email("test1@email.com")
                .pwd("password1")
                .name("Test User1")
                .build();

        UserDto userDto2 = UserDto.builder()
                .email("test2@email.com")
                .pwd("password2")
                .name("Test User2")
                .build();

        userService.createUser(userDto1);
        userService.createUser(userDto2);

        // When
        List<UserDto> result = userService.getUserByAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("test1@email.com", result.get(0).getEmail());
        assertEquals("Test User1", result.get(0).getName());
        assertEquals("test2@email.com", result.get(1).getEmail());
        assertEquals("Test User2", result.get(1).getName());
    }

    @Test
    void getUserById_존재하는_사용자() {
        // Given
        UserDto userDto = UserDto.builder()
                .email("test@email.com")
                .pwd("password")
                .name("Test User")
                .build();

        UserEntity user = userService.createUser(userDto);

        // When
        UserEntity result = userService.getUserById(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void getUserById_없는_사용자() {
        // Given
        Long userId = 99L;

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getUserByEmail_존재하는_사용자() {
        // Given
        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .email("test@email.com")
                .pwd("password")
                .name("Test User")
                .build();

        UserEntity userEntity = userService.createUser(userDto);

        // When
        UserDto result = userService.getUserByEmail(userEntity.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    void getUserByEmail_없는_사용자() {
        // Given
        String email = "notfound@example.com";

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }

    @Test
    void loadUserByUsername_성공() {
        // Given
        String email = "test@example.com";
        UserDto userDto = UserDto.builder()
                .email(email)
                .pwd("password")
                .name("Test User")
                .build();

        userService.createUser(userDto);

        // When
        UserDetails userDetails = userService.loadUserByUsername(email);

        // Then
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(userDto.getPwd(), userDetails.getPassword()));

    }

    @Test
    void loadUserByUsername_없는_사용자() {
        // Given
        String email = "notfound@example.com";

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }
}