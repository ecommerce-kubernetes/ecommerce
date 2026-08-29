package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.user.exception.UserErrorCode;
import com.example.userservice.support.annotation.IsolatedTest;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserBalanceResult;
import com.example.userservice.user.application.service.dto.result.UserIdentityResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.UserFixtureBuilder;
import com.example.userservice.user.domain.vo.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static com.example.userservice.user.fixture.UserCommandFixture.anAddShippingAddressCommand;
import static com.example.userservice.user.fixture.UserCommandFixture.anUserCreateCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@Transactional
class UserQueryServiceTest {

    @Autowired
    private UserQueryService userQueryService;
    @Autowired
    private UserCommandService userCommandService;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일과 비밀번호로 인증한다.")
    void authenticate() {
        //given
        UserCreateCommand createCommand = anUserCreateCommand().build();
        Long userId = userCommandService.createUser(createCommand);
        //when
        UserIdentityResult result = userQueryService.authenticate(createCommand.getEmail(), createCommand.getPassword());
        //then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("la9814@naver.com");
        assertThat(result.name()).isEqualTo("김이박");
        assertThat(result.role()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 인증하면 예외가 발생한다.")
    void authenticate_whenUserNotFound_thenThrownException() {
        //given
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
        UserCreateCommand createCommand = anUserCreateCommand().build();
        userCommandService.createUser(createCommand);
        //when
        //then
        assertThatThrownBy(() -> userQueryService.authenticate(createCommand.getEmail(), "wrongPassword1*"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.PASSWORD_NOT_MATCH);
    }

    @Test
    @DisplayName("유저 프로필을 조회한다.")
    void getUserProfile() {
        //given
        User user = UserFixtureBuilder.given().build();
        userRepository.save(user);
        userCommandService.addShippingAddress(anAddShippingAddressCommand().userId(user.getId()).build());
        //when
        UserProfileResult result = userQueryService.getUserProfile(user.getId());
        //then
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.userName()).isEqualTo(user.getName());
        assertThat(result.phoneNumber()).isEqualTo(user.getPhoneNumber());
        assertThat(result.availablePoints()).isEqualTo(Money.ZERO);
        assertThat(result.defaultShippingAddress().receiverName()).isEqualTo("수령인");
    }

    @Test
    @DisplayName("배송지가 없으면 유저 프로필의 대표 배송지는 null이다.")
    void getUserProfile_whenNoShippingAddress_thenDefaultShippingAddressIsNull() {
        //given
        User user = userRepository.save(UserFixtureBuilder.given().build());
        //when
        UserProfileResult result = userQueryService.getUserProfile(user.getId());
        //then
        assertThat(result.defaultShippingAddress()).isNull();
    }

    @Test
    @DisplayName("유저 프로필 조회시 유저를 찾을 수 없으면 예외가 발생한다.")
    void getUserProfile_whenUserNotFound_thenThrownException() {
        //given
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
        //when
        EmailAvailableResult result = userQueryService.checkAvailableEmail("notexist@naver.com");
        //then
        assertThat(result.available()).isTrue();
    }

    @Test
    @DisplayName("사용 불가능한 이메일이면 false를 반환한다.")
    void checkAvailableEmail_whenUnavailable_thenReturnsFalse() {
        //given
        User user = userRepository.save(UserFixtureBuilder.given().build());
        //when
        EmailAvailableResult result = userQueryService.checkAvailableEmail(user.getEmail());
        //then
        assertThat(result.available()).isFalse();
    }

    @Test
    @DisplayName("유저 포인트를 조회한다.")
    void getUserPoints() {
        //given
        User user = UserFixtureBuilder.given().build();
        userRepository.save(user);
        //when
        UserBalanceResult result = userQueryService.getUserPoints(user.getId());
        //then
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.availablePoints()).isEqualTo(Money.ZERO);
    }

    @Test
    @DisplayName("유저 포인트 조회시 유저를 찾을 수 없으면 예외가 발생한다.")
    void getUserPoints_whenUserNotFound_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> userQueryService.getUserPoints(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
}
