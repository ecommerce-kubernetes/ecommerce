package com.example.userservice.user.domain;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.domain.util.PasswordManager;
import com.example.userservice.user.domain.vo.Gender;
import com.example.userservice.user.domain.vo.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Mock
    private PasswordManager passwordManager;

    @Test
    @DisplayName("유저를 생성한다.")
    void createUser() {
        //given
        given(passwordManager.encrypt("password1234*")).willReturn("encryptedPassword");
        CreateUserContext context = CreateUserContext.builder()
                .email("la9814@naver.com")
                .password("password1234*")
                .name("김이박")
                .birthDate(LocalDate.of(1999, 12, 25))
                .gender(Gender.MALE)
                .phoneNumber("010-1234-5678")
                .build();
        //when
        User user = User.createUser(context, passwordManager, idGenerator);
        //then
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("la9814@naver.com");
        assertThat(user.getName()).isEqualTo("김이박");
        assertThat(user.getEncryptedPwd()).isEqualTo("encryptedPassword");
        assertThat(user.getGender()).isEqualTo(Gender.MALE);
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1999, 12, 25));
        assertThat(user.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(user.getPoint()).isEqualTo(Money.ZERO);
        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    @DisplayName("비밀번호가 일치하면 인증에 성공한다.")
    void authenticate() {
        //given
        User user = aUser();
        given(passwordManager.matches("password1234*", "encryptedPassword")).willReturn(true);
        //when
        //then
        assertThatCode(() -> user.authenticate("password1234*", passwordManager))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다.")
    void authenticate_whenPasswordNotMatch_thenThrownException() {
        //given
        User user = aUser();
        given(passwordManager.matches("wrongPassword", "encryptedPassword")).willReturn(false);
        //when
        //then
        assertThatThrownBy(() -> user.authenticate("wrongPassword", passwordManager))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.PASSWORD_NOT_MATCH);
    }

    @Test
    @DisplayName("포인트를 차감한다.")
    void deductPoint() {
        //given
        User user = aUser();
        user.refundPoint(Money.wons(10000L));
        //when
        user.deductPoint(Money.wons(3000L));
        //then
        assertThat(user.getPoint()).isEqualTo(Money.wons(7000L));
    }

    @Test
    @DisplayName("보유 포인트보다 많은 포인트를 차감하면 예외가 발생한다.")
    void deductPoint_whenInsufficientPoint_thenThrownException() {
        //given
        User user = aUser();
        user.refundPoint(Money.wons(1000L));
        //when
        //then
        assertThatThrownBy(() -> user.deductPoint(Money.wons(2000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.INSUFFICIENT_POINT);
    }

    @Test
    @DisplayName("포인트를 환불한다.")
    void refundPoint() {
        //given
        User user = aUser();
        //when
        user.refundPoint(Money.wons(5000L));
        //then
        assertThat(user.getPoint()).isEqualTo(Money.wons(5000L));
    }

    @Test
    @DisplayName("배송지를 추가한다.")
    void addShippingAddress() {
        //given
        User user = aUser();
        //when
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        //then
        assertThat(user.getShippingAddresses()).hasSize(1);

        ShippingAddress shippingAddress = user.getShippingAddresses().getFirst();
        assertThat(shippingAddress.getId()).isNotNull();
        assertThat(shippingAddress.getUser()).isEqualTo(user);
        assertThat(shippingAddress.getReceiverName()).isEqualTo("수령인");
    }

    @Test
    @DisplayName("첫번째로 추가한 배송지는 대표 배송지가 된다.")
    void addShippingAddress_whenFirstAddress_thenBecomesDefault() {
        //given
        User user = aUser();
        //when
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        //then
        assertThat(user.getShippingAddresses().getFirst().isDefault()).isTrue();
    }

    @Test
    @DisplayName("대표 배송지가 있는 상태에서 대표 여부를 지정하지 않고 배송지를 추가하면 기존 대표 배송지가 유지된다.")
    void addShippingAddress_whenDefaultExistsAndNotRequested_thenKeepsExistingDefault() {
        //given
        User user = aUser();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        //when
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        //then
        assertThat(user.getShippingAddresses()).hasSize(2);
        assertThat(firstAddress.isDefault()).isTrue();
        assertThat(user.getShippingAddresses().getLast().isDefault()).isFalse();
    }

    @Test
    @DisplayName("대표로 지정하여 배송지를 추가하면 기존 대표 배송지는 대표에서 해제된다.")
    void addShippingAddress_whenRequestedAsDefault_thenDemotesPreviousDefault() {
        //given
        User user = aUser();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        //when
        user.addShippingAddress(aShippingAddressContext(true), idGenerator);
        //then
        ShippingAddress newAddress = user.getShippingAddresses().getLast();
        assertThat(firstAddress.isDefault()).isFalse();
        assertThat(newAddress.isDefault()).isTrue();
    }

    @Test
    @DisplayName("배송지를 삭제한다.")
    void removeShippingAddress() {
        //given
        User user = aUser();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        ShippingAddress shippingAddress = user.getShippingAddresses().getFirst();
        //when
        user.removeShippingAddress(shippingAddress.getId());
        //then
        assertThat(user.getShippingAddresses()).isEmpty();
    }

    @Test
    @DisplayName("대표 배송지를 삭제하면 남은 배송지 중 하나가 새 대표 배송지가 된다.")
    void removeShippingAddress_whenDefaultRemoved_thenPromotesAnotherAddress() {
        //given
        User user = aUser();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        ShippingAddress secondAddress = user.getShippingAddresses().getLast();
        //when
        user.removeShippingAddress(firstAddress.getId());
        //then
        assertThat(user.getShippingAddresses()).containsExactly(secondAddress);
        assertThat(secondAddress.isDefault()).isTrue();
    }

    @Test
    @DisplayName("대표가 아닌 배송지를 삭제하면 기존 대표 배송지는 유지된다.")
    void removeShippingAddress_whenNonDefaultRemoved_thenDefaultUnchanged() {
        //given
        User user = aUser();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        ShippingAddress secondAddress = user.getShippingAddresses().getLast();
        //when
        user.removeShippingAddress(secondAddress.getId());
        //then
        assertThat(user.getShippingAddresses()).containsExactly(firstAddress);
        assertThat(firstAddress.isDefault()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 배송지를 삭제하면 예외가 발생한다.")
    void removeShippingAddress_whenNotFound_thenThrownException() {
        //given
        User user = aUser();
        //when
        //then
        assertThatThrownBy(() -> user.removeShippingAddress(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.SHIPPING_ADDRESS_NOT_FOUND);
    }

    @Test
    @DisplayName("배송지가 없으면 대표 배송지는 null이다.")
    void getDefaultShippingAddress_whenEmpty_thenReturnNull() {
        //given
        User user = aUser();
        //when
        ShippingAddress defaultShippingAddress = user.getDefaultShippingAddress();
        //then
        assertThat(defaultShippingAddress).isNull();
    }

    @Test
    @DisplayName("배송지가 있으면 대표 배송지를 반환한다.")
    void getDefaultShippingAddress_whenExists_thenReturnDefault() {
        //given
        User user = aUser();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        user.addShippingAddress(aShippingAddressContext(false), idGenerator);
        //when
        ShippingAddress defaultShippingAddress = user.getDefaultShippingAddress();
        //then
        assertThat(defaultShippingAddress).isEqualTo(firstAddress);
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

    private CreateShippingAddressContext aShippingAddressContext(boolean isDefault) {
        return CreateShippingAddressContext.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .isDefault(isDefault)
                .build();
    }
}
