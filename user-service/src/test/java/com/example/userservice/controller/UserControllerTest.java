package com.example.userservice.controller;

import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.AddressEntity;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.config.TestSecurityConfig;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestAddress;
import com.example.userservice.vo.RequestUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /users - 새로운 유저를 생성한다")
    public void testCreateUser() throws Exception {
        // Given
        RequestUser requestUser = new RequestUser("test@email.com", "password", "Test User");

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .encryptedPwd("Encrypt Password")
                .name("Test User")
                .build();

        when(userService.createUser(any(UserDto.class))).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("GET /users - 페이징된 유저 목록을 조회한다")
    public void testGetUsers() throws Exception {
        // Given
        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .email("user1@email.com")
                .pwd("pwd1")
                .name("User1")
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .email("user2@email.com")
                .pwd("pwd2")
                .name("User2")
                .build();

        List<UserDto> userList = Arrays.asList(userDto1, userDto2);
        Page<UserDto> userPage = new PageImpl<>(userList);

        when(userService.getUserByAll(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].userId").value(1L))
                .andExpect(jsonPath("$.content[0].email").value("user1@email.com"))
                .andExpect(jsonPath("$.content[0].name").value("User1"))
                .andExpect(jsonPath("$.content[1].userId").value(2L))
                .andExpect(jsonPath("$.content[1].email").value("user2@email.com"))
                .andExpect(jsonPath("$.content[1].name").value("User2"));
    }

    @Test
    @DisplayName("GET /users/{userId} - 특정 유저 정보를 가져온다")
    public void testGetUser() throws Exception {
        // Given
        Long userId = 1L;

        List<AddressDto> addressDtoList = new ArrayList<>();

        UserDto userDto = UserDto.builder()
                .id(userId)
                .name("User")
                .email("user1@email.com")
                .createdAt(LocalDateTime.now())
                .addresses(addressDtoList)
                .cache(0)
                .point(0)
                .build();

        when(userService.getUserById(userId)).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@email.com"))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses").isEmpty());

    }

    @Test
    @DisplayName("POST /address/{userId} - 배송지 추가")
    void testCreateAddress() throws Exception {
        RequestAddress request = new RequestAddress("집", "서울시 강남구", "101호", true);
        Long userId = 1L;

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .encryptedPwd("Encrypt Password")
                .name("Test User")
                .addresses(new ArrayList<>())
                .cache(0)
                .point(0)
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

        mockMvc.perform(post("/address/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses[0].name").value("집"));
    }

    @Test
    @DisplayName("PATCH /address/{userId}/{addressId} - 기본 배송지 수정")
    void testEditDefaultAddress() throws Exception {
        // Given
        Long userId = 1L;
        Long addressId1 = 1L;
        Long addressId2 = 2L;

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .encryptedPwd("encodedPwd")
                .name("테스트유저")
                .addresses(new ArrayList<>())
                .cache(0)
                .point(0)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        AddressEntity address1 = AddressEntity.builder()
                .name("집")
                .address("서울시 강남구")
                .details("101호")
                .defaultAddress(true)
                .user(userEntity)
                .build();
        ReflectionTestUtils.setField(address1, "id", addressId1);

        AddressEntity address2 = AddressEntity.builder()
                .name("회사")
                .address("수원시 장안구")
                .details("201호")
                .defaultAddress(false)
                .user(userEntity)
                .build();
        ReflectionTestUtils.setField(address2, "id", addressId2);

        userEntity.getAddresses().add(address1);
        userEntity.getAddresses().add(address2);

        // Mocking
        when(userService.editDefaultAddress(userId, addressId1)).thenReturn(userEntity);

        // When & Then
        mockMvc.perform(patch("/address/{userId}/{addressId}", userId, addressId1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses.length()").value(2));
    }

    @Test
    @DisplayName("DELETE /address/{userId}/{addressId} - 배송지 삭제")
    void testDeleteAddress() throws Exception {
        RequestAddress request = new RequestAddress("집", "서울시 강남구", "101호", true);
        Long userId = 1L;
        Long addressId = 1L;

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .encryptedPwd("Encrypt Password")
                .name("Test User")
                .addresses(new ArrayList<>())
                .cache(0)
                .point(0)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        AddressEntity address = AddressEntity.builder()
                .name(request.getName())
                .address(request.getAddress())
                .details(request.getDetails())
                .defaultAddress(request.isDefaultAddress())
                .user(userEntity)
                .build();
        ReflectionTestUtils.setField(address, "id", addressId);

        when(userService.deleteAddress(userId, addressId)).thenReturn(userEntity);

        mockMvc.perform(delete("/address/{userId}/{addressId}", userId, addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.addresses").isArray());
    }

    @Test
    @DisplayName("PATCH /cache/recharge/{userId}/{amount} - 캐시 충전")
    void testRechargeCache() throws Exception {
        Long userId = 1L;
        int amount = 5000;

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .encryptedPwd("encrypt")
                .name("User")
                .cache(amount)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.rechargeCache(userId, amount)).thenReturn(userEntity);

        mockMvc.perform(patch("/cache/recharge/{userId}/{amount}", userId, amount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.cache").value(amount));
    }

    @Test
    @DisplayName("PATCH /cache/deduct/{userId}/{amount} - 캐시 차감")
    void testDeductCache() throws Exception {
        Long userId = 1L;
        int amount = 3000;

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .encryptedPwd("encrypt")
                .name("User")
                .cache(7000)  // 차감 후 값
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.deductCache(userId, amount)).thenReturn(userEntity);

        mockMvc.perform(patch("/cache/deduct/{userId}/{amount}", userId, amount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.cache").value(7000));
    }

    @Test
    @DisplayName("PATCH /point/recharge/{userId}/{amount} - 포인트 충전")
    void testRechargePoint() throws Exception {
        Long userId = 1L;
        int amount = 10000;

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .encryptedPwd("encrypt")
                .name("User")
                .point(amount)
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.rechargePoint(userId, amount)).thenReturn(userEntity);

        mockMvc.perform(patch("/point/recharge/{userId}/{amount}", userId, amount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(amount));
    }

    @Test
    @DisplayName("PATCH /point/deduct/{userId}/{amount} - 포인트 차감")
    void testDeductPoint() throws Exception {
        Long userId = 1L;
        int amount = 2000;

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .encryptedPwd("encrypt")
                .name("User")
                .point(8000)  // 차감 후 값
                .build();
        ReflectionTestUtils.setField(userEntity, "id", userId);

        when(userService.deductPoint(userId, amount)).thenReturn(userEntity);

        mockMvc.perform(patch("/point/deduct/{userId}/{amount}", userId, amount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(8000));
    }
}

