package com.example.userservice.user.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.UserFixtureBuilder;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.util.PasswordManager;
import com.example.userservice.user.exception.UserErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.userservice.user.fixture.UserCommandFixture.anAddShippingAddressCommand;
import static com.example.userservice.user.fixture.UserCommandFixture.anUserCreateCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordManager passwordManager;

    private UserCommandService userCommandService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        userCommandService = new UserCommandService(userRepository, passwordManager, idGenerator, new UserContextFactory());
    }

    @Test
    @DisplayName("회원을 생성한다.")
    void create() {
        //given
        UserCreateCommand command = anUserCreateCommand().build();
        given(userRepository.existsByEmail(command.getEmail())).willReturn(false);
        given(passwordManager.encrypt(command.getPassword())).willReturn("encryptedPwd");
        //when
        Long userId = userCommandService.createUser(command);
        //then
        assertThat(userId).isNotNull();

        then(userRepository).should().save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getId()).isEqualTo(userId);
        assertThat(savedUser.getEmail()).isEqualTo(command.getEmail());
        assertThat(savedUser.getName()).isEqualTo(command.getName());
        assertThat(savedUser.getEncryptedPwd()).isEqualTo("encryptedPwd");
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 예외가 발생하고 회원을 생성하지 않는다.")
    void create_whenEmailAlreadyExists_thenThrownException() {
        //given
        UserCreateCommand command = anUserCreateCommand().build();
        given(userRepository.existsByEmail(command.getEmail())).willReturn(true);
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
        User user = UserFixtureBuilder.given().build();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        AddShippingAddressCommand command = anAddShippingAddressCommand().userId(user.getId()).build();
        //when
        userCommandService.addShippingAddress(command);
        //then
        assertThat(user.getShippingAddresses()).hasSize(1);
        assertThat(user.getShippingAddresses().getFirst().getReceiverName()).isEqualTo(command.receiverName());
        assertThat(user.getShippingAddresses().getFirst().isDefault()).isTrue();
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("대표 배송지로 요청하여 배송지를 추가하면 기존 대표 배송지는 해제되고 새 배송지가 대표가 된다.")
    void addShippingAddress_whenRequestedAsDefault_thenNewAddressBecomesDefault() {
        //given
        User user = UserFixtureBuilder.given().build();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        userCommandService.addShippingAddress(anAddShippingAddressCommand().userId(user.getId()).build());

        AddShippingAddressCommand command = anAddShippingAddressCommand().userId(user.getId()).isDefault(true).build();
        //when
        userCommandService.addShippingAddress(command);
        //then
        assertThat(user.getShippingAddresses()).hasSize(2);
        assertThat(user.getShippingAddresses().getFirst().isDefault()).isFalse();
        assertThat(user.getShippingAddresses().getLast().isDefault()).isTrue();
    }

    @Test
    @DisplayName("배송지 추가시 유저를 찾을 수 없으면 예외가 발생한다.")
    void addShippingAddress_whenUserNotFound_thenThrownException() {
        //given
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        AddShippingAddressCommand command = anAddShippingAddressCommand().userId(999L).build();
        //when
        //then
        assertThatThrownBy(() -> userCommandService.addShippingAddress(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("배송지를 삭제한다.")
    void deleteShippingAddress() {
        //given
        User user = UserFixtureBuilder.given().build();
        user.addShippingAddress(aShippingAddressContext(), idGenerator);
        Long shippingAddressId = user.getShippingAddresses().getFirst().getId();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
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
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> userCommandService.deleteShippingAddress(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
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
