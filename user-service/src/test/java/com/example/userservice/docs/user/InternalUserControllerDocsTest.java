package com.example.userservice.docs.user;

import com.example.userservice.docs.RestDocsSupport;
import com.example.userservice.user.adapter.in.web.InternalUserController;
import com.example.userservice.user.application.service.UserService;
import com.example.userservice.user.application.service.dto.result.UserBalanceResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import static com.example.userservice.user.fixture.UserResultFixture.anUserPointsResult;
import static com.example.userservice.user.fixture.UserResultFixture.anUserProfileResult;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalUserControllerDocsTest extends RestDocsSupport {

    private UserService userService = Mockito.mock(UserService.class);

    @Override
    protected Object initController() {
        return new InternalUserController(userService);
    }

    @Test
    @DisplayName("유저 프로필 정보를 조회한다")
    void getUserProfile() throws Exception {
        //given
        UserProfileResult result = anUserProfileResult().build();
        given(userService.getUserProfile(anyLong())).willReturn(result);
        //when
        //then
        mockMvc.perform(get("/internal/users/{userId}/profile", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("internal-user/profile",
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("userId").description("조회할 유저 id(식별자)")
                                ),
                                responseFields(UserDescriptor.userProfileResponse())
                        )
                );
    }

    @Test
    @DisplayName("유저 포인트 정보를 조회한다")
    void getUserPoints() throws Exception {
        //given
        UserBalanceResult result = anUserPointsResult().build();
        given(userService.getUserPoints(anyLong())).willReturn(result);
        //when
        //then
        mockMvc.perform(get("/internal/users/{userId}/points", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("internal-user/points",
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("userId").description("조회할 유저 id(식별자)")
                                ),
                                responseFields(UserDescriptor.userPointsResponse())
                        )
                );
    }
}
