package com.example.userservice.user.domain;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.domain.vo.Gender;
import com.example.userservice.common.domain.vo.Role;
import com.example.userservice.user.exception.ShippingAddressErrorCode;
import com.example.userservice.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("유저를 생성한다.")
    void create() {
        //given
        CreateUserContext context = CreateUserContext.builder()
                .id(idGenerator.generate())
                .email("la9814@naver.com")
                .encryptedPassword("encryptedPassword")
                .name("김이박")
                .birthDate(LocalDate.of(1999, 12, 25))
                .gender(Gender.MALE)
                .phoneNumber("010-1234-5678")
                .build();
        //when
        User user = User.create(context);
        //then
        assertThat(user.getId()).isEqualTo(context.id());
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
    @DisplayName("배송지를 추가한다.")
    void addShippingAddress() {
        //given
        User user = UserFixtureBuilder.given().build();
        //when
        user.addShippingAddress(aShippingAddressContext(false));
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
        User user = UserFixtureBuilder.given().build();
        //when
        user.addShippingAddress(aShippingAddressContext(false));
        //then
        assertThat(user.getShippingAddresses().getFirst().isDefault()).isTrue();
    }

    @Test
    @DisplayName("대표 배송지가 있는 상태에서 대표 여부를 지정하지 않고 배송지를 추가하면 기존 대표 배송지가 유지된다.")
    void addShippingAddress_whenDefaultExistsAndNotRequested_thenKeepsExistingDefault() {
        //given
        User user = UserFixtureBuilder.given().build();
        user.addShippingAddress(aShippingAddressContext(false));
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        //when
        user.addShippingAddress(aShippingAddressContext(false));
        //then
        assertThat(user.getShippingAddresses()).hasSize(2);
        assertThat(firstAddress.isDefault()).isTrue();
        assertThat(user.getShippingAddresses().getLast().isDefault()).isFalse();
    }

    @Test
    @DisplayName("대표로 지정하여 배송지를 추가하면 기존 대표 배송지는 대표에서 해제된다.")
    void addShippingAddress_whenRequestedAsDefault_thenDemotesPreviousDefault() {
        //given
        User user = UserFixtureBuilder.given().build();
        user.addShippingAddress(aShippingAddressContext(false));
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        //when
        user.addShippingAddress(aShippingAddressContext(true));
        //then
        ShippingAddress newAddress = user.getShippingAddresses().getLast();
        assertThat(firstAddress.isDefault()).isFalse();
        assertThat(newAddress.isDefault()).isTrue();
    }

    @Test
    @DisplayName("배송지를 삭제한다.")
    void removeShippingAddress() {
        //given
        User user = UserFixtureBuilder.given().build();
        user.addShippingAddress(aShippingAddressContext(false));
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
        User user = UserFixtureBuilder.given().build();
        user.addShippingAddress(aShippingAddressContext(false));
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        user.addShippingAddress(aShippingAddressContext(false));
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
        User user = UserFixtureBuilder.given().build();
        user.addShippingAddress(aShippingAddressContext(false));
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        user.addShippingAddress(aShippingAddressContext(false));
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
        User user = UserFixtureBuilder.given().build();
        //when
        //then
        assertThatThrownBy(() -> user.removeShippingAddress(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ShippingAddressErrorCode.SHIPPING_ADDRESS_NOT_FOUND);
    }

    @Test
    @DisplayName("배송지가 없으면 대표 배송지는 null이다.")
    void getDefaultShippingAddress_whenEmpty_thenReturnNull() {
        //given
        User user = UserFixtureBuilder.given().build();
        //when
        ShippingAddress defaultShippingAddress = user.getDefaultShippingAddress();
        //then
        assertThat(defaultShippingAddress).isNull();
    }

    @Test
    @DisplayName("배송지가 있으면 대표 배송지를 반환한다.")
    void getDefaultShippingAddress_whenExists_thenReturnDefault() {
        //given
        User user = UserFixtureBuilder.given().build();
        user.addShippingAddress(aShippingAddressContext(false));
        ShippingAddress firstAddress = user.getShippingAddresses().getFirst();
        user.addShippingAddress(aShippingAddressContext(false));
        //when
        ShippingAddress defaultShippingAddress = user.getDefaultShippingAddress();
        //then
        assertThat(defaultShippingAddress).isEqualTo(firstAddress);
    }

    @Test
    @DisplayName("포인트를 추가한다.")
    void addPoints(){
        //given
        User user = UserFixtureBuilder.given().build();
        //when
        user.addPoints(Money.wons(1000L));
        //then
        assertThat(user.getPoint()).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("포인트를 차감한다.")
    void deductPoints(){
        //given
        User user = UserFixtureBuilder.given().build();
        user.addPoints(Money.wons(1000L));
        //when
        user.deductPoints(Money.wons(1000L));
        //then
        assertThat(user.getPoint()).isEqualTo(Money.ZERO);
    }

    @Test
    @DisplayName("포인트를 차감할때 보유 포인트가 부족하면 예외가 발생한다.")
    void deductPoints_whenInsufficientPoint_thenThrownException(){
        //given
        User user = UserFixtureBuilder.given().build();
        //when
        //then
        assertThatThrownBy(() -> user.deductPoints(Money.wons(1000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.INSUFFICIENT_POINTS);
    }

    private CreateShippingAddressContext aShippingAddressContext(boolean isDefault) {
        return CreateShippingAddressContext.builder()
                .id(idGenerator.generate())
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .isDefault(isDefault)
                .build();
    }
}
