package com.example.userservice.service;

import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.AddressEntity;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(0, result.getCache());
        assertEquals(0, result.getPoint());
    }

    @Test
    void createUser_실패_이미존재할경우() {
        // Given
        UserDto userDto = UserDto.builder()
                .email("test@email.com")
                .pwd("password")
                .name("Test User")
                .build();

        // When
        userService.createUser(userDto);


        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDto));
        assertEquals("이미 등록된 이메일입니다.", exception.getMessage());
    }

    @Test
    void getUserByAll_성공() {
        // Given
        userService.createUser(UserDto.builder()
                .email("user1@example.com")
                .pwd("1234")
                .name("User1")
                .build());

        userService.createUser(UserDto.builder()
                .email("user2@example.com")
                .pwd("5678")
                .name("User2")
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<UserDto> resultPage = userService.getUserByAll(pageable);
        List<UserDto> result = resultPage.getContent();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void getUserById_성공() {
        // Given
        UserEntity savedUser = userService.createUser(UserDto.builder()
                .email("test@email.com")
                .pwd("password")
                .name("Test")
                .build());

        // When
        UserDto result = userService.getUserById(savedUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_실패_없는ID() {
        // Given
        Long invalidId = 999L;

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserById(invalidId));

        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void getUserByEmail_성공() {
        // Given
        UserDto userDto = UserDto.builder()
                .email("test@email.com")
                .pwd("password")
                .name("Test")
                .build();

        userService.createUser(userDto);

        // When
        UserDto result = userService.getUserByEmail(userDto.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    void getUserByEmail_실패_없는이메일() {
        // Given
        String email = "nonexistent@email.com";

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserByEmail(email));

        assertTrue(exception.getMessage().contains("User not found with email"));
    }

    @Test
    void loadUserByUsername_성공() {
        // Given
        String email = "user@email.com";
        String rawPwd = "password";
        userService.createUser(UserDto.builder()
                .email(email)
                .pwd(rawPwd)
                .name("사용자")
                .build());

        // When
        UserDetails userDetails = userService.loadUserByUsername(email);

        // Then
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(rawPwd, userDetails.getPassword()));
    }

    @Test
    void loadUserByUsername_실패_없는이메일() {
        // Given
        String email = "notfound@email.com";

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(email));

        assertTrue(exception.getMessage().contains("User not found with username"));
    }

    @Test
    void addAddressByUserId_성공_기본주소_설정() {
        // Given
        UserEntity user = userService.createUser(UserDto.builder()
                .email("addr@test.com")
                .pwd("password")
                .name("주소 사용자")
                .build());

        AddressDto addressDto = AddressDto.builder()
                .name("집")
                .address("서울시 강남구")
                .details("101동 202호")
                .defaultAddress(true)
                .build();

        // When
        UserEntity updatedUser = userService.addAddressByUserId(user.getId(), addressDto);

        // Then
        assertEquals(1, updatedUser.getAddresses().size());
        assertEquals("서울시 강남구", updatedUser.getAddresses().get(0).getAddress());
        assertEquals("101동 202호", updatedUser.getAddresses().get(0).getDetails());
        assertTrue(updatedUser.getAddresses().get(0).isDefaultAddress());
    }

    @Test
    void editDefaultAddress_성공_다른주소로변경() {
        // Given
        UserEntity user = userService.createUser(UserDto.builder()
                .email("default@test.com")
                .pwd("password")
                .name("기본 주소 변경")
                .build());

        AddressDto addr1 = AddressDto.builder()
                .addressId(1L)
                .name("집")
                .address("서울 A")
                .details("101")
                .defaultAddress(true)
                .build();

        AddressDto addr2 = AddressDto.builder()
                .addressId(2L)
                .name("회사")
                .address("서울 B")
                .details("202")
                .defaultAddress(false)
                .build();

        user = userService.addAddressByUserId(user.getId(), addr1);
        user = userService.addAddressByUserId(user.getId(), addr2);

        // When
        UserEntity updated = userService.editDefaultAddress(user.getId(), 2L);

        // Then
        assertEquals(1, updated.getAddresses().stream().filter(AddressEntity::isDefaultAddress).count());
        assertFalse(updated.getAddresses().get(0).isDefaultAddress());
        assertTrue(updated.getAddresses().get(1).isDefaultAddress());
    }

    @Test
    void deleteAddress_성공() {
        // Given

        UserEntity user = userService.createUser(UserDto.builder()
                .email("delete@test.com")
                .pwd("password")
                .name("삭제 사용자")
                .build());

        AddressDto addressDto = AddressDto.builder()
                .addressId(1L)
                .name("삭제주소")
                .address("서울 삭제")
                .details("삭제 상세")
                .defaultAddress(true)
                .build();

        user = userService.addAddressByUserId(user.getId(), addressDto);

        // When
        UserEntity result = userService.deleteAddress(user.getId(), user.getAddresses().get(0).getId());

        // Then
        assertTrue(result.getAddresses().isEmpty());
    }

    @Test
    void rechargeCache_성공() {
        // Given
        UserEntity user = userService.createUser(UserDto.builder()
                .email("cache@test.com")
                .pwd("password")
                .name("캐시 사용자")
                .cache(0)
                .point(0)
                .build());

        // When
        UserEntity updated = userService.rechargeCache(user.getId(), 5000);

        // Then
        assertEquals(5000, updated.getCache());
    }

    @Test
    void rechargeCache_실패_음수입력() {
        // Given
        UserEntity user = userService.createUser(UserDto.builder()
                .email("cachefail@test.com")
                .pwd("password")
                .name("캐시 실패")
                .cache(0)
                .point(0)
                .build());

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.rechargeCache(user.getId(), -1000));
        assertEquals("충전 금액은 0보다 커야 합니다.", ex.getMessage());
    }

    @Test
    void deductCache_성공() {
        // Given
        UserEntity user = userService.createUser(UserDto.builder()
                .email("cacheuse@test.com")
                .pwd("password")
                .name("차감 사용자")
                .cache(0)
                .point(0)
                .build());

        userService.rechargeCache(user.getId(), 3000);

        // When
        UserEntity updated = userService.deductCache(user.getId(), 1000);

        // Then
        assertEquals(2000, updated.getCache());
    }

    @Test
    void deductCache_실패_부족() {
        // Given
        UserEntity user = userService.createUser(UserDto.builder()
                .email("nocash@test.com")
                .pwd("password")
                .name("부족 사용자")
                .cache(0)
                .point(0)
                .build());

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.deductCache(user.getId(), 500));
        assertEquals("금액이 모자릅니다.", ex.getMessage());
    }

    @Test
    void rechargePoint_성공() {
        // Given
        UserEntity user = userService.createUser(UserDto.builder()
                .email("point@test.com")
                .pwd("password")
                .name("포인트 사용자")
                .cache(0)
                .point(0)
                .build());

        // When
        UserEntity updated = userService.rechargePoint(user.getId(), 7000);

        // Then
        assertEquals(7000, updated.getPoint());
    }

    @Test
    void deductPoint_실패_음수요청() {
        // Given
        UserEntity user = userService.createUser(UserDto.builder()
                .email("negpoint@test.com")
                .pwd("password")
                .name("음수 포인트")
                .cache(0)
                .point(0)
                .build());

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.deductPoint(user.getId(), -1));
        assertEquals("차감 금액은 0보다 커야 합니다.", ex.getMessage());
    }

}