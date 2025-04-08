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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

//    @Test
//    @DisplayName("GET /users - 전체 유저 목록을 조회한다")
//    public void testGetUsers() throws Exception {
//        // Given
//        UserDto userDto1 = UserDto.builder()
//                .id(1L)
//                .email("user1@email.com")
//                .pwd("pwd1")
//                .name("User1")
//                .build();
//
//        UserDto userDto2 = UserDto.builder()
//                .id(2L)
//                .email("user2@email.com")
//                .pwd("pwd2")
//                .name("User2")
//                .build();
//
//        List<UserDto> userList = Arrays.asList(userDto1, userDto2);
//
//        when(userService.getUserByAll()).thenReturn(userList);
//
//        // When & Then
//        mockMvc.perform(get("/users"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].userId").value(1L))
//                .andExpect(jsonPath("$[0].email").value("user1@email.com"))
//                .andExpect(jsonPath("$[0].name").value("User1"))
//                .andExpect(jsonPath("$[1].userId").value(2L))
//                .andExpect(jsonPath("$[1].email").value("user2@email.com"))
//                .andExpect(jsonPath("$[1].name").value("User2"));
//    }
//
//    @Test
//    @DisplayName("GET /users/{userId} - 특정 유저 정보를 가져온다")
//    public void testGetUser() throws Exception {
//        // Given
//        Long userId = 1L;
//
//        UserEntity userEntity = UserEntity.builder()
//                .email("user1@email.com")
//                .name("User")
//                .build();
//
//        when(userService.getUserById(userId)).thenReturn(Optional.of(userEntity));
//
//        // When & Then
//        mockMvc.perform(get("/users/{userId}", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.email").value("user1@email.com"))
//                .andExpect(jsonPath("$.name").value("User"));
//    }

}