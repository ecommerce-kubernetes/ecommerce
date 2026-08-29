package com.example.userservice.docs.user;

import com.example.userservice.common.security.model.UserPrincipal;
import com.example.userservice.docs.RestDocsSupport;
import com.example.userservice.user.adapter.in.web.UserController;
import com.example.userservice.user.adapter.in.web.dto.AddShippingAddressRequest;
import com.example.userservice.user.adapter.in.web.dto.UserCreateRequest;
import com.example.userservice.user.application.service.UserCommandService;
import com.example.userservice.user.application.service.UserQueryService;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.AddShippingAddressResult;
import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserCreateResult;
import com.example.userservice.user.domain.vo.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.example.userservice.user.fixture.UserRequestFixture.anAddShippingAddressRequest;
import static com.example.userservice.user.fixture.UserRequestFixture.anUserCreateRequest;
import static com.example.userservice.user.fixture.UserResultFixture.anAddShippingAddressResult;
import static com.example.userservice.user.fixture.UserResultFixture.anEmailAvailableResult;
import static com.example.userservice.user.fixture.UserResultFixture.anUserCreateResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerDocsTest extends RestDocsSupport {

    private UserCommandService userCommandService = Mockito.mock(UserCommandService.class);
    private UserQueryService userQueryService = Mockito.mock(UserQueryService.class);

    @Override
    protected Object initController() {
        return new UserController(userCommandService, userQueryService);
    }

    @Override
    protected HandlerMethodArgumentResolver[] getArgumentResolvers() {
        return new HandlerMethodArgumentResolver[]{
                new PageableHandlerMethodArgumentResolver(),
                new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(UserPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return UserPrincipal.of(1L, Role.ROLE_USER);
                    }
                }
        };
    }

    @Test
    @DisplayName("유저 생성")
    void createUser() throws Exception {
        //given
        UserCreateRequest request = anUserCreateRequest().build();
        UserCreateResult result = anUserCreateResult().build();
        given(userCommandService.createUser(any(UserCreateCommand.class))).willReturn(result);
        //when
        //then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("user/create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(UserDescriptor.userCreateRequest()),
                                responseFields(UserDescriptor.userCreateResponse())
                        )
                );
    }

    @Test
    @DisplayName("이메일 사용 가능 여부 확인")
    void checkEmailAvailable() throws Exception {
        //given
        EmailAvailableResult result = anEmailAvailableResult().build();
        given(userQueryService.checkAvailableEmail(anyString())).willReturn(result);
        //when
        //then
        mockMvc.perform(get("/users/email-availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@naver.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("user/email-availability",
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                        parameterWithName("email").description("확인할 이메일")
                                ),
                                responseFields(UserDescriptor.emailAvailableResponse())
                        )
                );
    }

    @Test
    @DisplayName("배송지 추가")
    void addShippingAddress() throws Exception {
        //given
        AddShippingAddressRequest request = anAddShippingAddressRequest().build();
        AddShippingAddressResult result = anAddShippingAddressResult().build();
        given(userCommandService.addShippingAddress(any(AddShippingAddressCommand.class))).willReturn(result);
        //when
        //then
        mockMvc.perform(post("/users/shipping-addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("user/shipping-address-create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(UserDescriptor.addShippingAddressRequest()),
                                responseFields(UserDescriptor.addShippingAddressResponse())
                        )
                );
    }

    @Test
    @DisplayName("배송지 삭제")
    void deleteShippingAddress() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/users/shipping-addresses/{shippingAddressId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("user/shipping-address-delete",
                                pathParameters(
                                        parameterWithName("shippingAddressId").description("삭제할 배송지 id(식별자)")
                                )
                        )
                );
    }
}
