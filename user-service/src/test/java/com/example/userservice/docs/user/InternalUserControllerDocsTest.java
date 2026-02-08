package com.example.userservice.docs.user;

import com.example.userservice.api.user.controller.InternalUserController;
import com.example.userservice.api.user.service.UserService;
import com.example.userservice.api.user.service.dto.result.UserOrderResponse;
import com.example.userservice.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import static com.example.userservice.api.support.fixture.UserResponseFixture.anUserOrderResponse;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InternalUserControllerDocsTest extends RestDocsSupport {

    private UserService userService = Mockito.mock(UserService.class);

    @Override
    protected Object initController() {
        return new InternalUserController(userService);
    }

    @Test
    @DisplayName("유저 주문 정보를 조회한다")
    void getUserInfoForOrder() throws Exception {
        //given
        UserOrderResponse response = anUserOrderResponse().build();
        given(userService.getUserInfoForOrder(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/internal/users/{userId}/order-info", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("internal-get-user",
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("userId").description("조회할 유저 ID")
                                ),
                                responseFields(
                                        fieldWithPath("userId").description("유저 ID"),
                                        fieldWithPath("pointBalance").description("포인트 잔액"),
                                        fieldWithPath("userName").description("유저 이름"),
                                        fieldWithPath("phoneNumber").description("010-1234-5678")
                                )
                        )
                );
    }
}
