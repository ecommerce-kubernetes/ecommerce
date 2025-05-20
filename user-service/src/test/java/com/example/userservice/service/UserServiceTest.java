package com.example.userservice.service;

import com.example.userservice.advice.exceptions.InvalidAmountException;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .email("test@example.com")
                .encryptedPwd("encryptedPwd")
                .name("John")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("01012345678")
                .cache(100)
                .point(50)
                .role(Role.ROLE_USER)
                .build();

        ReflectionTestUtils.setField(userEntity, "id", 1L);
    }

    @Test
    @DisplayName("POST /users - 회원 생성 성공")
    void createUser_success() {
        UserDto dto = UserDto.builder()
                .email("test@example.com")
                .pwd("rawPwd")
                .name("John")
                .gender("MALE")
                .birthDate("1990-01-01")
                .phoneNumber("01012345678")
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(dto.getPwd())).thenReturn("encryptedPwd");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserEntity savedUser = userService.createUser(dto);

        assertEquals(dto.getEmail(), savedUser.getEmail());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("GET /users/{id} - 회원 조회 성공")
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        UserDto dto = userService.getUserById(1L);

        assertEquals("test@example.com", dto.getEmail());
        assertEquals("John", dto.getName());
    }

    @Test
    @DisplayName("PUT /users/{id} - 회원 정보 수정 성공")
    void updateUser_success() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Updated Name")
                .pwd("newPwd")
                .phoneNumber("01099998888")
                .gender("FEMALE")
                .birthDate("1991-02-02")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(bCryptPasswordEncoder.encode("newPwd")).thenReturn("encodedNewPwd");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserEntity updated = userService.updateUser(dto);

        assertEquals("Updated Name", updated.getName());
        assertEquals("encodedNewPwd", updated.getEncryptedPwd());
    }

    @Test
    @DisplayName("POST /users/{id}/cache - 캐시 충전 성공")
    void rechargeCache_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserEntity updated = userService.rechargeCache(1L, 100);

        assertEquals(200, updated.getCache());
    }

    @Test
    @DisplayName("POST /users/{id}/cache - 0 이하 금액 충전 시 예외 발생")
    void rechargeCache_invalidAmount_throwsException() {
        assertThrows(InvalidAmountException.class, () -> userService.rechargeCache(1L, 0));
    }

    @Test
    @DisplayName("POST /users/{id}/cache/deduct - 잔액 부족 시 예외 발생")
    void deductCache_insufficientFunds_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        assertThrows(InvalidAmountException.class, () -> userService.deductCache(1L, 200));
    }

    @Test
    @DisplayName("POST /users/check - 비밀번호 일치 시 성공")
    void checkUser_passwordMatch_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(bCryptPasswordEncoder.matches("rawPwd", "encryptedPwd")).thenReturn(true);

        assertDoesNotThrow(() -> userService.checkUser("test@example.com", "rawPwd"));
    }

    @Test
    @DisplayName("GET /users/email/{email} - 이메일로 회원 조회 성공")
    void getUserByEmail_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));

        UserDto result = userService.getUserByEmail("test@example.com");

        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("DELETE /users/{id} - 회원 삭제 성공")
    void deleteUser_success() {
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }
}