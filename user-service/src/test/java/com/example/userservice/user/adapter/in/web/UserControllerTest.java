package com.example.userservice.user.adapter.in.web;

import com.example.userservice.support.security.annotation.WithCustomMockUser;
import com.example.userservice.support.security.config.TestSecurityConfig;
import com.example.userservice.user.adapter.in.web.dto.AddShippingAddressRequest;
import com.example.userservice.user.adapter.in.web.dto.UserCreateRequest;
import com.example.userservice.user.application.service.UserCommandService;
import com.example.userservice.user.application.service.UserQueryService;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.AddShippingAddressResult;
import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserCreateResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.example.userservice.user.fixture.UserRequestFixture.anAddShippingAddressRequest;
import static com.example.userservice.user.fixture.UserRequestFixture.anUserCreateRequest;
import static com.example.userservice.user.fixture.UserResultFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private UserQueryService userQueryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원을 생성한다")
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
                .andExpect(jsonPath("$.userId").value(String.valueOf(result.userId())));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("회원 생성 요청 검증")
    @MethodSource("provideInvalidCreateRequest")
    void createUser_validation(String description, UserCreateRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    @DisplayName("사용 가능한 이메일 여부를 확인한다")
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
                .andExpect(jsonPath("$.available").value(result.available()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidEmailAvailableParam")
    @DisplayName("사용 가능 이메일 파라미터 검증")
    void checkEmailAvailable_validation(String description, String email, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/users/email-availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", email))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/users/email-availability"));
    }

    @Test
    @DisplayName("배송지를 추가한다")
    @WithCustomMockUser
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
                .andExpect(jsonPath("$.userId").value(String.valueOf(result.userId())));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("배송지 추가 요청 검증")
    @MethodSource("provideInvalidAddShippingAddressRequest")
    @WithCustomMockUser
    void addShippingAddress_validation(String description, AddShippingAddressRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/users/shipping-addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/users/shipping-addresses"));
    }

    @Test
    @DisplayName("배송지를 삭제한다")
    @WithCustomMockUser
    void deleteShippingAddress() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/users/shipping-addresses/{shippingAddressId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }


    static Stream<Arguments> provideInvalidCreateRequest() {
        return Stream.of(
                Arguments.of("이메일이 누락되면 예외가 발생한다.",
                        anUserCreateRequest().email(null).build(),
                        "email", "이메일은 필수 입력값입니다"),

                Arguments.of("이메일 형식이 잘못되면 예외가 발생한다.",
                        anUserCreateRequest().email("invalidEmail").build(),
                        "email", "올바른 이메일 형식을 입력해주세요"),

                Arguments.of("비밀번호가 누락되면 예외가 발생한다.",
                        anUserCreateRequest().password(null).build(),
                        "password", "비밀번호는 필수 입력값입니다"),

                Arguments.of("비밀번호 형식이 잘못되면 예외가 발생한다.",
                        anUserCreateRequest().password("asdf").build(),
                        "password", "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다"),

                Arguments.of("이름이 누락되면 예외가 발생한다.",
                        anUserCreateRequest().name(null).build(),
                        "name", "이름은 필수 입력값입니다"),
                Arguments.of("이름 형식이 잘못되면 예외가 발생한다.",
                        anUserCreateRequest().name("이").build(),
                        "name", "이름은 2글자~12글자 사이여야 합니다"),

                Arguments.of("생년월일이 누락되면 예외가 발생한다.",
                        anUserCreateRequest().birthDate(null).build(),
                        "birthDate", "생년월일은 필수 입력값입니다"),

                Arguments.of("성별이 누락되면 예외가 발생한다.",
                        anUserCreateRequest().gender(null).build(),
                        "gender", "성별은 필수 입력값입니다"),
                Arguments.of("성별 형식이 잘못되면 예외가 발생한다.",
                        anUserCreateRequest().gender("남자").build(),
                        "gender", "성별은 MALE 또는 FEMALE 이어야 합니다"),

                Arguments.of("전호번호가 누락되면 예외가 발생한다.",
                        anUserCreateRequest().phoneNumber(null).build(),
                        "phoneNumber", "전화번호는 필수 입력값 입니다"),

                Arguments.of("전화번호 형식이 잘못되면 예외가 발생한다.",
                        anUserCreateRequest().phoneNumber("01012345678").build(),
                        "phoneNumber", "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
        );
    }

    static Stream<Arguments> provideInvalidEmailAvailableParam() {
        return Stream.of(
                Arguments.of("이메일 파라미터가 누락되면 예외가 발생한다.", "", "이메일은 필수 입력값입니다"),
                Arguments.of("이메일 파라미터가 잘못된 이메일 형식이면 예외가 발생한다.", "invalidEmail", "올바른 이메일 형식을 입력해주세요")
        );
    }

    static Stream<Arguments> provideInvalidAddShippingAddressRequest() {
        return Stream.of(
                Arguments.of("수령인 이름이 누락되면 예외가 발생한다.",
                        anAddShippingAddressRequest().receiverName(null).build(),
                        "receiverName", "수령인 이름은 필수 입력값입니다"),
                Arguments.of("수령인 전화번호가 누락되면 예외가 발생한다.",
                        anAddShippingAddressRequest().receiverPhone(null).build(),
                        "receiverPhone", "수령인 전화번호는 필수 입력값입니다"),
                Arguments.of("잘못된 수령인 전화번호 형식",
                        anAddShippingAddressRequest().receiverPhone("01012345678").build(),
                        "receiverPhone", "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)"),
                Arguments.of("우편번호가 누락되면 예외가 발생한다.",
                        anAddShippingAddressRequest().zipCode(null).build(),
                        "zipCode", "우편번호는 필수 입력값입니다"),
                Arguments.of("잘못된 우편번호 형식이면 예외가 발생한다.",
                        anAddShippingAddressRequest().zipCode("123").build(),
                        "zipCode", "우편번호는 5자리 숫자여야 합니다"),
                Arguments.of("주소가 없으면 예외가 발생한다.",
                        anAddShippingAddressRequest().address(null).build(),
                        "address", "주소는 필수 입력값입니다"),
                Arguments.of("상세주소가 없으면 예외가 발생한다.",
                        anAddShippingAddressRequest().addressDetail(null).build(),
                        "addressDetail", "상세주소는 필수 입력값입니다")
        );
    }
}
