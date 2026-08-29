package com.example.userservice.user.adapter.in.web;

import com.example.userservice.support.security.config.TestSecurityConfig;
import com.example.userservice.user.application.service.UserQueryService;
import com.example.userservice.user.application.service.dto.result.UserBalanceResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.userservice.user.fixture.UserResultFixture.anUserPointsResult;
import static com.example.userservice.user.fixture.UserResultFixture.anUserProfileResult;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = InternalUserController.class)
class InternalUserControllerTest {

    @MockitoBean
    private UserQueryService userQueryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유저 프로필 정보를 조회한다")
    void getUserProfile() throws Exception {
        //given
        UserProfileResult result = anUserProfileResult().build();
        given(userQueryService.getUserProfile(anyLong())).willReturn(result);
        //when
        //then
        mockMvc.perform(get("/internal/users/{userId}/profile", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(result.userId()))
                .andExpect(jsonPath("$.userName").value(result.userName()))
                .andExpect(jsonPath("$.phoneNumber").value(result.phoneNumber()))
                .andExpect(jsonPath("$.availablePoints").value(result.availablePoints()))
                .andExpect(jsonPath("$.defaultShippingAddress.receiverName").value(result.defaultShippingAddress().receiverName()));
    }

    @Test
    @DisplayName("유저 포인트 정보를 조회한다")
    void getUserPoints() throws Exception {
        //given
        UserBalanceResult result = anUserPointsResult().build();
        given(userQueryService.getUserPoints(anyLong())).willReturn(result);
        //when
        //then
        mockMvc.perform(get("/internal/users/{userId}/points", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(result.userId()))
                .andExpect(jsonPath("$.availablePoints").value(result.availablePoints()));
    }
}
