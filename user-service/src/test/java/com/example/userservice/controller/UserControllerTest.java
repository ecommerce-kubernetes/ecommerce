package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.security.TestSecurityConfig;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
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

        UserDto userDto = UserDto.builder()
                .id(userId)
                .name("User")
                .email("user1@email.com")
                .createAt(LocalDate.now())
                .addresses(null)
                .build();

        when(userService.getUserById(userId)).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@email.com"))
                .andExpect(jsonPath("$.name").value("User"));
    }

}