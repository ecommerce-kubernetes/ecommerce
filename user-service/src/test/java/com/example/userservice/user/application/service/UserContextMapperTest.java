package com.example.userservice.user.application.service;

import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.CreateUserCommand;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.example.userservice.user.fixture.UserCommandFixture.anAddShippingAddressCommand;
import static com.example.userservice.user.fixture.UserCommandFixture.anUserCreateCommand;
import static org.assertj.core.api.Assertions.assertThat;

class UserContextMapperTest {

    private final UserContextMapper contextFactory = new UserContextMapper();

    @Test
    @DisplayName("회원 생성 커맨드로 회원 생성 컨텍스트를 생성한다.")
    void createUserContext() {
        //given
        CreateUserCommand command = anUserCreateCommand().build();
        Long id = 1L;
        String encryptedPassword = "encryptedPassword";
        //when
        CreateUserContext context = contextFactory.createUserContext(id, command, encryptedPassword);
        //then
        assertThat(context)
                .extracting("id", "email", "encryptedPassword", "name", "birthDate", "gender", "phoneNumber")
                .containsExactly(
                        id, command.email(), encryptedPassword, command.name(),
                        command.birthDate(), command.gender(), command.phoneNumber()
                );
    }

    @Test
    @DisplayName("배송지 추가 커맨드로 배송지 생성 컨텍스트를 생성한다.")
    void createShippingAddressContext() {
        //given
        AddShippingAddressCommand command = anAddShippingAddressCommand().isDefault(true).build();
        Long id = 1L;
        //when
        CreateShippingAddressContext context = contextFactory.createShippingAddressContext(id, command);
        //then
        assertThat(context)
                .extracting("id", "receiverName", "receiverPhone", "zipCode", "address", "addressDetail", "isDefault")
                .containsExactly(
                        id, command.receiverName(), command.receiverPhone(), command.zipCode(),
                        command.address(), command.addressDetail(), command.isDefault()
                );
    }
}
