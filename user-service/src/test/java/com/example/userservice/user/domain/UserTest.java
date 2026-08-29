package com.example.userservice.user.domain;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
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
        ShippingAddress shippingAddress = user.addShippingAddress("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호", idGenerator);
        //then
        assertThat(shippingAddress.getId()).isNotNull();
        assertThat(shippingAddress.getUser()).isEqualTo(user);
        assertThat(user.getShippingAddresses()).containsExactly(shippingAddress);
    }

    @Test
    @DisplayName("배송지를 삭제한다.")
    void removeShippingAddress() {
        //given
        User user = aUser();
        ShippingAddress shippingAddress = user.addShippingAddress("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호", idGenerator);
        //when
        user.removeShippingAddress(shippingAddress.getId());
        //then
        assertThat(user.getShippingAddresses()).isEmpty();
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
    @DisplayName("배송지가 있으면 첫번째로 등록된 배송지를 대표 배송지로 반환한다.")
    void getDefaultShippingAddress_whenExists_thenReturnFirst() {
        //given
        User user = aUser();
        ShippingAddress firstAddress = user.addShippingAddress("첫번째 수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호", idGenerator);
        user.addShippingAddress("두번째 수령인", "010-9876-5432", "54321",
                "서울시 강남대로 456", "456동 4567호", idGenerator);
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
}
