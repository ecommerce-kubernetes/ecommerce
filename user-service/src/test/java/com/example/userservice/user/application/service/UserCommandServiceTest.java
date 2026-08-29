package com.example.userservice.user.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.support.annotation.IsolatedTest;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.UserFixtureBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static com.example.userservice.user.fixture.UserCommandFixture.anAddShippingAddressCommand;
import static com.example.userservice.user.fixture.UserCommandFixture.anUserCreateCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@Transactional
class UserCommandServiceTest {

    @Autowired
    private UserCommandService userCommandService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원을 생성한다.")
    void create() {
        //given
        UserCreateCommand command = anUserCreateCommand().build();
        //when
        Long userId = userCommandService.createUser(command);
        //then
        assertThat(userId).isNotNull();

        User savedUser = userRepository.findById(userId).orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo(command.getEmail());
        assertThat(savedUser.getName()).isEqualTo(command.getName());
        assertThat(passwordEncoder.matches(command.getPassword(), savedUser.getEncryptedPwd())).isTrue();
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 예외가 발생하고 회원을 생성하지 않는다.")
    void create_whenEmailAlreadyExists_thenThrownException() {
        //given
        User existingUser = userRepository.save(UserFixtureBuilder.given().build());
        UserCreateCommand command = anUserCreateCommand().email(existingUser.getEmail()).build();
        //when
        //then
        assertThatThrownBy(() -> userCommandService.createUser(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("배송지를 추가한다.")
    void addShippingAddress() {
        //given
        User user = userRepository.save(UserFixtureBuilder.given().build());
        AddShippingAddressCommand command = anAddShippingAddressCommand().userId(user.getId()).build();
        //when
        userCommandService.addShippingAddress(command);
        //then
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getShippingAddresses()).hasSize(1);
        assertThat(savedUser.getShippingAddresses().getFirst().getReceiverName()).isEqualTo(command.receiverName());
        assertThat(savedUser.getShippingAddresses().getFirst().isDefault()).isTrue();
    }

    @Test
    @DisplayName("대표 배송지로 요청하여 배송지를 추가하면 기존 대표 배송지는 해제되고 새 배송지가 대표가 된다.")
    void addShippingAddress_whenRequestedAsDefault_thenNewAddressBecomesDefault() {
        //given
        User user = userRepository.save(UserFixtureBuilder.given().build());
        userCommandService.addShippingAddress(anAddShippingAddressCommand().userId(user.getId()).build());

        AddShippingAddressCommand command = anAddShippingAddressCommand().userId(user.getId()).isDefault(true).build();
        //when
        userCommandService.addShippingAddress(command);
        //then
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getShippingAddresses()).hasSize(2);
        assertThat(savedUser.getShippingAddresses().getFirst().isDefault()).isFalse();
        assertThat(savedUser.getShippingAddresses().getLast().isDefault()).isTrue();
    }

    @Test
    @DisplayName("배송지 추가시 유저를 찾을 수 없으면 예외가 발생한다.")
    void addShippingAddress_whenUserNotFound_thenThrownException() {
        //given
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
        User user = userRepository.save(UserFixtureBuilder.given().build());
        userCommandService.addShippingAddress(anAddShippingAddressCommand().userId(user.getId()).build());
        Long shippingAddressId = userRepository.findById(user.getId()).orElseThrow()
                .getShippingAddresses().getFirst().getId();
        //when
        userCommandService.deleteShippingAddress(user.getId(), shippingAddressId);
        //then
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getShippingAddresses()).isEmpty();
    }

    @Test
    @DisplayName("배송지 삭제시 유저를 찾을 수 없으면 예외가 발생한다.")
    void deleteShippingAddress_whenUserNotFound_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> userCommandService.deleteShippingAddress(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
}
