package com.example.userservice.docs.user;

import com.example.userservice.api.user.controller.UserController;
import com.example.userservice.api.user.controller.dto.UserCreateRequest;
import com.example.userservice.api.user.service.UserService;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import com.example.userservice.api.user.service.dto.result.UserCreateResponse;
import com.example.userservice.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.example.userservice.api.support.fixture.UserRequestFixture.anUserCreateRequest;
import static com.example.userservice.api.support.fixture.UserResponseFixture.anUserCreateResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerDocsTest extends RestDocsSupport {
    private UserService userService = mock(UserService.class);

    @Override
    protected Object initController() {
        return new UserController(userService);
    }

    @Test
    @DisplayName("유저 생성")
    void createUser() throws Exception {
        //given
        UserCreateRequest request = anUserCreateRequest().build();
        UserCreateResponse response = anUserCreateResponse().build();
        given(userService.createUser(any(UserCreateCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(
                        document(
                                "user-create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("email").description("이메일").optional(),
                                        fieldWithPath("password").description("비밀번호").optional(),
                                        fieldWithPath("name").description("이름").optional(),
                                        fieldWithPath("birthDate").description("생년월일").optional(),
                                        fieldWithPath("gender").description("성별").optional(),
                                        fieldWithPath("phoneNumber").description("전화번호").optional()
                                ),

                                responseFields(
                                        fieldWithPath("id").description("유저 id(식별자)"),
                                        fieldWithPath("email").description("이메일"),
                                        fieldWithPath("name").description("이름"),
                                        fieldWithPath("birthDate").description("생년월일"),
                                        fieldWithPath("gender").description("성별"),
                                        fieldWithPath("phoneNumber").description("전화번호")
                                )
                        )
                );

    }
}
