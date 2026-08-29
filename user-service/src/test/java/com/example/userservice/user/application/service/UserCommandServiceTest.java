package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.UserCreateResult;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.domain.util.PasswordManager;
import com.example.userservice.user.domain.vo.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.example.userservice.user.fixture.UserCommandFixture.anAddShippingAddressCommand;
import static com.example.userservice.user.fixture.UserCommandFixture.anUserCreateCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @InjectMocks
    private UserCommandService userCommandService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordManager passwordManager;
    @Spy
    private IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("회원을 생성한다.")
    void createUser() {
        //given
        UserCreateCommand command = anUserCreateCommand().build();
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordManager.encrypt(anyString())).willReturn("encryptedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        //when
        UserCreateResult result = userCommandService.createUser(command);
        //then
        assertThat(result.userId()).isNotNull();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(command.getEmail());
        assertThat(captor.getValue().getEncryptedPwd()).isEqualTo("encryptedPassword");
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 예외가 발생하고 회원을 생성하지 않는다.")
    void createUser_whenEmailAlreadyExists_thenThrownException() {
        //given
        UserCreateCommand command = anUserCreateCommand().build();
        given(userRepository.existsByEmail(anyString())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> userCommandService.createUser(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS);

        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("배송지를 추가한다.")
    void addShippingAddress() {
        //given
        User user = aUser();
        AddShippingAddressCommand command = anAddShippingAddressCommand().userId(user.getId()).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        //when
        userCommandService.addShippingAddress(command);
        //then
        assertThat(user.getShippingAddresses()).hasSize(1);
        assertThat(user.getShippingAddresses().getFirst().getReceiverName()).isEqualTo(command.receiverName());
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("배송지 추가시 유저를 찾을 수 없으면 예외가 발생한다.")
    void addShippingAddress_whenUserNotFound_thenThrownException() {
        //given
        AddShippingAddressCommand command = anAddShippingAddressCommand().build();
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> userCommandService.addShippingAddress(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("배송지를 삭제한다.")
    void deleteShippingAddress() {
        //given
        User user = aUser();
        user.addShippingAddress(aShippingAddressContext(), idGenerator);
        Long shippingAddressId = user.getShippingAddresses().getFirst().getId();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        //when
        userCommandService.deleteShippingAddress(user.getId(), shippingAddressId);
        //then
        assertThat(user.getShippingAddresses()).isEmpty();
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("배송지 삭제시 유저를 찾을 수 없으면 예외가 발생한다.")
    void deleteShippingAddress_whenUserNotFound_thenThrownException() {
        //given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> userCommandService.deleteShippingAddress(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("포인트를 차감한다.")
    void deductPoints() {
        //given
        User user = aUser();
        user.refundPoint(Money.wons(5000L));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        //when
        userCommandService.deductPoints(user.getId(), 3000L);
        //then
        assertThat(user.getPoint()).isEqualTo(Money.wons(2000L));
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("포인트를 환불한다.")
    void refundPoints() {
        //given
        User user = aUser();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        //when
        userCommandService.refundPoints(user.getId(), 3000L);
        //then
        assertThat(user.getPoint()).isEqualTo(Money.wons(3000L));
        then(userRepository).should().save(user);
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
