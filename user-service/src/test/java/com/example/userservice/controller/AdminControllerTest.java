package com.example.userservice.controller;

import com.example.userservice.config.TestSecurityConfig;
import com.example.userservice.dto.AddressDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.entity.AddressEntity;
import com.example.userservice.jpa.entity.UserEntity;
import com.example.userservice.service.TokenService;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestCreateAddress;
import com.example.userservice.vo.RequestEditAddress;
import com.example.userservice.vo.RequestEditUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AdminController 단위 테스트")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .name("John")
                .cache(100)
                .point(50)
                .createdAt(LocalDateTime.now())
                .addresses(Collections.emptyList())
                .build();

        userEntity = UserEntity.builder()
                .email("test@example.com")
                .name("John")
                .cache(100)
                .point(50)
                .addresses(new ArrayList<>())
                .build();

        ReflectionTestUtils.setField(userEntity, "id", 1L);
    }

    @Test
    @DisplayName("GET /users - 전체 유저 조회")
    void getUsers_returnsPagedUsers() throws Exception {
        Page<UserDto> page = new PageImpl<>(List.of(userDto));
        when(userService.getUserByAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(1L))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /users/{userid} - 특정 userid 유저 조회")
    void getUserById_returnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/admin/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    @DisplayName("PATCH /users/{userId} - 유저 정보 수정 성공")
    void updateUser_success() throws Exception {
        RequestEditUser request = new RequestEditUser(
                "Password1!",
                "ValidName",
                "1990-01-01",
                "MALE",
                "01012345678"
        );

        when(userService.updateUser(any(UserDto.class))).thenReturn(userEntity);

        mockMvc.perform(patch("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @DisplayName("DELETE /users/{userId} - 유저 삭제 성공")
    void deleteUser_success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("POST /users/{userId}/address - 배송지 추가 성공")
    void createAddress_success() throws Exception {
        RequestCreateAddress request = new RequestCreateAddress("집", "서울시 강남구", "101동 202호", true);

        AddressEntity addressEntity = AddressEntity.builder()
                .name("집")
                .address("서울시 강남구")
                .details("101동 202호")
                .defaultAddress(true)
                .build();

        ReflectionTestUtils.setField(addressEntity, "id", 1L);

        userEntity.getAddresses().add(addressEntity);
        when(userService.addAddressByUserId(eq(1L), any(AddressDto.class))).thenReturn(userEntity);

        mockMvc.perform(post("/admin/users/1/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.addresses[0].name").value("집"));
    }

    @Test
    @DisplayName("PATCH /users/{userId}/address - 배송지 수정 성공")
    void updateAddress_success() throws Exception {
        RequestEditAddress request = new RequestEditAddress(1L, "회사", "서울시 서초구", "3층", false);

        AddressEntity updatedAddress = AddressEntity.builder()
                .name("회사")
                .address("서울시 서초구")
                .details("3층")
                .defaultAddress(false)
                .build();

        ReflectionTestUtils.setField(updatedAddress, "id", 1L);

        userEntity.getAddresses().add(updatedAddress);
        when(userService.updateAddress(eq(1L), any(AddressDto.class))).thenReturn(userEntity);

        mockMvc.perform(patch("/admin/users/1/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.addresses[0].name").value("회사"));
    }

    @Test
    @DisplayName("DELETE /admin/users/{userId}/address/{addressId} - 배송지 삭제 성공")
    void deleteAddress_success() throws Exception {
        AddressEntity updatedAddress = AddressEntity.builder()
                .name("회사")
                .address("서울시 서초구")
                .details("3층")
                .defaultAddress(false)
                .build();
        ReflectionTestUtils.setField(updatedAddress, "id", 1L);

        userEntity.getAddresses().add(updatedAddress);

        when(userService.deleteAddress(eq(1L), eq(1L))).thenReturn(userEntity);

        mockMvc.perform(delete("/admin/users/1/address/1")) // ✅ 올바른 URL
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    @DisplayName("PATCH /users/{userId}/cache/recharge/{amount} - 유저 캐시 충전 성공")
    void rechargeCache_success() throws Exception {
        when(userService.rechargeCache(1L, 100)).thenReturn(userEntity);

        mockMvc.perform(patch("/admin/users/1/cache/recharge/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.cache").value(100));
    }

    @Test
    @DisplayName("PATCH /users/{userId}/cache/deduct/{amount} - 유저 캐시 차감 성공")
    void deductCache_success() throws Exception {
        when(userService.deductCache(1L, 50)).thenReturn(userEntity);

        mockMvc.perform(patch("/admin/users/1/cache/deduct/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.cache").value(100));
    }

    @Test
    @DisplayName("PATCH /users/{userId}/point/recharge/{amount} - 포인트 충전 성공")
    void rechargePoint_success() throws Exception {
        userEntity.rechargePoint(100);
        when(userService.rechargePoint(1L, 100)).thenReturn(userEntity);

        mockMvc.perform(patch("/admin/users/1/point/recharge/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.point").value(userEntity.getPoint()));
    }

    @Test
    @DisplayName("PATCH /users/{userId}/point/deduct/{amount} - 포인트 차감 성공")
    void deductPoint_success() throws Exception {
        userEntity.deductPoint(20);
        when(userService.deductPoint(1L, 20)).thenReturn(userEntity);

        mockMvc.perform(patch("/admin/users/1/point/deduct/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.point").value(userEntity.getPoint()));
    }
}