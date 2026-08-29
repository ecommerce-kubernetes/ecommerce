package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserBalanceResult;
import com.example.userservice.user.application.service.dto.result.UserIdentityResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.domain.util.PasswordManager;
import com.example.userservice.user.domain.vo.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @InjectMocks
    private UserQueryService userQueryService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordManager passwordManager;

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("이메일과 비밀번호로 인증한다.")
    void authenticate() {
        //given
        User user = aUser();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordManager.matches(anyString(), anyString())).willReturn(true);
        //when
        UserIdentityResult result = userQueryService.authenticate(user.getEmail(), "password1234*");
        //then
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.role()).isEqualTo(user.getRole());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 인증하면 예외가 발생한다.")
    void authenticate_whenUserNotFound_thenThrownException() {
        //given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> userQueryService.authenticate("notfound@naver.com", "password1234*"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다.")
    void authenticate_whenPasswordNotMatch_thenThrownException() {
        //given
        User user = aUser();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordManager.matches(anyString(), anyString())).willReturn(false);
        //when
        //then
        assertThatThrownBy(() -> userQueryService.authenticate(user.getEmail(), "wrongPassword"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.PASSWORD_NOT_MATCH);
    }

    @Test
    @DisplayName("유저 프로필을 조회한다.")
    void getUserProfile() {
        //given
        User user = aUser();
        user.refundPoint(Money.wons(10000L));
        user.addShippingAddress(aShippingAddressContext(), idGenerator);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        //when
        UserProfileResult result = userQueryService.getUserProfile(user.getId());
        //then
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.userName()).isEqualTo(user.getName());
        assertThat(result.phoneNumber()).isEqualTo(user.getPhoneNumber());
        assertThat(result.availablePoints()).isEqualTo(10000L);
        assertThat(result.defaultShippingAddress().receiverName()).isEqualTo("수령인");
    }

    @Test
    @DisplayName("배송지가 없으면 유저 프로필의 대표 배송지는 null이다.")
    void getUserProfile_whenNoShippingAddress_thenDefaultShippingAddressIsNull() {
        //given
        User user = aUser();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        //when
        UserProfileResult result = userQueryService.getUserProfile(user.getId());
        //then
        assertThat(result.defaultShippingAddress()).isNull();
    }

    @Test
    @DisplayName("유저 프로필 조회시 유저를 찾을 수 없으면 예외가 발생한다.")
    void getUserProfile_whenUserNotFound_thenThrownException() {
        //given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> userQueryService.getUserProfile(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용 가능한 이메일이면 true를 반환한다.")
    void checkAvailableEmail_whenAvailable_thenReturnsTrue() {
        //given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        //when
        EmailAvailableResult result = userQueryService.checkAvailableEmail("test@naver.com");
        //then
        assertThat(result.available()).isTrue();
    }

    @Test
    @DisplayName("사용 불가능한 이메일이면 false를 반환한다.")
    void checkAvailableEmail_whenUnavailable_thenReturnsFalse() {
        //given
        given(userRepository.existsByEmail(anyString())).willReturn(true);
        //when
        EmailAvailableResult result = userQueryService.checkAvailableEmail("test@naver.com");
        //then
        assertThat(result.available()).isFalse();
    }

    @Test
    @DisplayName("유저 포인트를 조회한다.")
    void getUserPoints() {
        //given
        User user = aUser();
        user.refundPoint(Money.wons(7000L));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        //when
        UserBalanceResult result = userQueryService.getUserPoints(user.getId());
        //then
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.availablePoints()).isEqualTo(7000L);
    }

    @Test
    @DisplayName("유저 포인트 조회시 유저를 찾을 수 없으면 예외가 발생한다.")
    void getUserPoints_whenUserNotFound_thenThrownException() {
        //given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> userQueryService.getUserPoints(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    private User aUser() {
        given(passwordManager.encrypt(anyString())).willReturn("encryptedPassword");
        CreateUserContext context = CreateUserContext.builder()
                .email("la9814@naver.com")
                .password("password1234*")
                .name("김이박")
                .birthDate(LocalDate.of(1999, 12, 25))
                .gender(Gender.MALE)
                .phoneNumber("010-1234-5678")
                .build();

        return User.createUser(context, passwordManager, idGenerator);
    }

    private CreateShippingAddressContext aShippingAddressContext() {
        return CreateShippingAddressContext.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .isDefault(false)
                .build();
    }
}
