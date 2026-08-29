package com.example.userservice.user.application.service;

import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.example.userservice.user.fixture.UserCommandFixture.anAddShippingAddressCommand;
import static com.example.userservice.user.fixture.UserCommandFixture.anUserCreateCommand;
import static org.assertj.core.api.Assertions.assertThat;

class UserContextFactoryTest {

    private final UserContextFactory contextFactory = new UserContextFactory();

    @Test
    @DisplayName("회원 생성 커맨드로 회원 생성 컨텍스트를 생성한다.")
    void createContext() {
        //given
        UserCreateCommand command = anUserCreateCommand().build();
        //when
        CreateUserContext context = contextFactory.createUserContext(command);
        //then
        assertThat(context)
                .extracting("email", "password", "name", "birthDate", "gender", "phoneNumber")
                .containsExactly(
                        command.getEmail(), command.getPassword(), command.getName(),
                        command.getBirthDate(), command.getGender(), command.getPhoneNumber()
                );
    }

    @Test
    @DisplayName("배송지 추가 커맨드로 배송지 생성 컨텍스트를 생성한다.")
    void createShippingAddressContext() {
        //given
        AddShippingAddressCommand command = anAddShippingAddressCommand().isDefault(true).build();
        //when
        CreateShippingAddressContext context = contextFactory.createShippingAddressContext(command);
        //then
        assertThat(context)
                .extracting("receiverName", "receiverPhone", "zipCode", "address", "addressDetail", "isDefault")
                .containsExactly(
                        command.receiverName(), command.receiverPhone(), command.zipCode(),
                        command.address(), command.addressDetail(), command.isDefault()
                );
    }
}
