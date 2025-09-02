package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.entity.AddressEntity;
import com.example.userservice.jpa.entity.Gender;
import com.example.userservice.jpa.entity.Role;
import com.example.userservice.jpa.entity.UserEntity;
import com.example.userservice.config.TestSecurityConfig;
import com.example.userservice.service.TokenService;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@DisplayName("UserController 단위 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenService tokenService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /V1__create_users_table.sql - 새로운 유저를 생성한다")
    public void testCreateUser() throws Exception {
        // Given
        RequestCreateUser request = new RequestCreateUser(
                "test@example.com", "Password1!", "Test User",
                "1999-04-13","MALE", "01012345678", false
        );

        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPassword")
                .phoneNumber("01012345678")
                .phoneVerified(false)
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(0)
                .point(0)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();

        ReflectionTestUtils.setField(userEntity, "id", 1L);

        when(userService.createUser(any(UserDto.class))).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(post("/V1__create_users_table.sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.birthDate").value("1999-04-13"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.phoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.phoneVerified").value(false))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /V1__create_users_table.sql/confirm-password - 비밀번호 확인")
    public void testConfirmPassword() throws Exception {
        // Given
        RequestLoginUser request = new RequestLoginUser("test@example.com", "Password1!");

        // userService.checkUser(...)은 void 메서드이므로 doNothing() 사용
        doNothing().when(userService).checkUser(eq("test@example.com"), eq("Password1!"));

        // When & Then
        mockMvc.perform(post("/V1__create_users_table.sql/confirm-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /V1__create_users_table.sql/update - 유저 정보 수정")
    public void testUpdateUser() throws Exception {
        // Given
        Long userId = 1L;
        RequestEditUser request = new RequestEditUser("Password1!", "Test User", "1999-04-13",
                "MALE", "01012345678");

        // updateUser 엔드포인트는 ResponseBody 없이 상태 코드만 반환
        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedNewPassword")
                .phoneNumber("01012345678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(1000)
                .point(500)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.updateUser(any(UserDto.class))).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(patch("/V1__create_users_table.sql/update")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /address - 배송지 조회")
    public void testGetAddressesWithHeader() throws Exception {
        // Given
        Long userId = 1L;

        AddressEntity address = AddressEntity.builder()
                .name("집")
                .address("서울시 강남구")
                .details("101호")
                .defaultAddress(true)
                .build();
        ReflectionTestUtils.setField(address, "id", 10L);

        List<AddressEntity> addressList = List.of(address);
        when(userService.getAddressesByUserId(userId)).thenReturn(addressList);

        // When & Then
        mockMvc.perform(get("/V1__create_users_table.sql/address")
                        .header("X-User-Id", String.valueOf(userId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses[0].addressId").value(10L))
                .andExpect(jsonPath("$.addresses[0].name").value("집"))
                .andExpect(jsonPath("$.addresses[0].address").value("서울시 강남구"))
                .andExpect(jsonPath("$.addresses[0].details").value("101호"))
                .andExpect(jsonPath("$.addresses[0].defaultAddress").value(true));
    }

    @Test
    @DisplayName("POST /V1__create_users_table.sql/address - 배송지 추가")
    public void testCreateAddress() throws Exception {
        // Given
        Long userId = 1L;
        RequestCreateAddress request = new RequestCreateAddress("회사", "수원시 장안구", "201호", false);

        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(0)
                .point(0)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        AddressEntity address = AddressEntity.builder()
                .name(request.getName())
                .address(request.getAddress())
                .details(request.getDetails())
                .defaultAddress(request.isDefaultAddress())
                .user(userEntity)
                .build();
        userEntity.getAddresses().add(address);

        when(userService.addAddressByUserId(eq(userId), any())).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(post("/V1__create_users_table.sql/address")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses[0].name").value("회사"));
    }

    @Test
    @DisplayName("PATCH /V1__create_users_table.sql/address - 배송지 정보 수정")
    public void testUpdateAddress() throws Exception {
        // Given
        Long userId = 1L;
        RequestEditAddress request = new RequestEditAddress(1L,"새주소", "부산시 해운대구", "301호", true);

        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(0)
                .point(0)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        // 주소 수정 후 반영 예시 (기존 주소 목록 대체)
        AddressEntity updatedAddress = AddressEntity.builder()
                .name(request.getName())
                .address(request.getAddress())
                .details(request.getDetails())
                .defaultAddress(request.isDefaultAddress())
                .user(userEntity)
                .build();
        ReflectionTestUtils.setField(updatedAddress, "id", 20L);
        userEntity.getAddresses().add(updatedAddress);

        when(userService.updateAddress(eq(userId), any())).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(patch("/V1__create_users_table.sql/address")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses[0].name").value("새주소"));
    }

    @Test
    @DisplayName("DELETE /V1__create_users_table.sql/address/{addressName} - 배송지 삭제")
    public void testDeleteAddress() throws Exception {
        // Given
        Long userId = 1L;
        Long addressId = 1L;

        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(0)
                .point(0)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.deleteAddress(userId, addressId)).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(delete("/V1__create_users_table.sql/address/{addressId}", addressId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.addresses").isArray());
    }

    @Test
    @DisplayName("PATCH /V1__create_users_table.sql/cache/recharge/{amount} - 캐시 충전")
    public void testRechargeCache() throws Exception {
        // Given
        Long userId = 1L;
        int amount = 5000;
        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(amount)
                .point(0)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.rechargeCache(userId, amount)).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(patch("/V1__create_users_table.sql/cache/recharge/{amount}", amount)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.cache").value(amount));
    }

    @Test
    @DisplayName("PATCH /V1__create_users_table.sql/cache/deduct/{amount} - 캐시 차감")
    public void testDeductCache() throws Exception {
        // Given
        Long userId = 1L;
        int amount = 3000;
        // 차감 후 캐시가 7000이라 가정
        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(7000)
                .point(0)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.deductCache(userId, amount)).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(patch("/V1__create_users_table.sql/cache/deduct/{amount}", amount)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.cache").value(7000));
    }

    @Test
    @DisplayName("PATCH /V1__create_users_table.sql/point/recharge/{amount} - 포인트 충전")
    public void testRechargePoint() throws Exception {
        // Given
        Long userId = 1L;
        int amount = 10000;
        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(0)
                .point(amount)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.rechargePoint(userId, amount)).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(patch("/V1__create_users_table.sql/point/recharge/{amount}", amount)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(amount));
    }

    @Test
    @DisplayName("PATCH /V1__create_users_table.sql/point/deduct/{amount} - 포인트 차감")
    public void testDeductPoint() throws Exception {
        // Given
        Long userId = 1L;
        int amount = 2000;
        // 차감 후 포인트가 8000이라 가정
        UserEntity userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPassword")
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("1999-04-13"))
                .cache(0)
                .point(8000)
                .addresses(new ArrayList<>())
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.deductPoint(userId, amount)).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(patch("/V1__create_users_table.sql/point/deduct/{amount}", amount)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(8000));
    }

    @Test
    @DisplayName("POST /V1__create_users_table.sql/refresh-token - 엑세스 토큰 재발급")
    public void testRefreshToken() throws Exception {
        // Given
        String refreshToken = "dummyRefreshToken";
        String newAccessToken = "newAccessToken";
        // HttpServletRequest 의 쿠키에 refresh_token을 포함시키기 위해 Cookie 객체 생성
        Cookie cookie = new Cookie("refresh_token", refreshToken);

        when(tokenService.reissueAccessToken(refreshToken)).thenReturn(newAccessToken);

        // When & Then
        mockMvc.perform(post("/V1__create_users_table.sql/refresh-token")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(header().string("token", newAccessToken));
    }

    @Test
    @DisplayName("POST /V1__create_users_table.sql/logout - 로그아웃")
    public void testLogout() throws Exception {
        // Given
        Long userId = 1L;
        // 로그아웃 후 쿠키 삭제를 위해 ResponseCookie 헤더가 설정된 상태 확인
        doNothing().when(tokenService).deleteRefreshToken(userId);

        // When & Then
        mockMvc.perform(post("/V1__create_users_table.sql/logout")
                        .header("X-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 완료"));
    }

    @Test
    @DisplayName("DELETE /V1__create_users_table.sql - 회원탈퇴")
    public void testDeleteUser() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/V1__create_users_table.sql")
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent());
    }
}

