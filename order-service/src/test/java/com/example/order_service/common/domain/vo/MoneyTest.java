package com.example.order_service.common.domain.vo;

import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MoneyTest {

    @Test
    @DisplayName("금액 값 객체를 생성한다")
    void money() {
        //given
        //when
        Money wons = Money.wons(1000L);
        //then
        assertThat(wons.longValue()).isEqualTo(1000);
    }

    @Test
    @DisplayName("금액이 같으면 같은 값이다")
    void money_equal_amount() {
        //given
        Money money1 = Money.wons(1000L);
        Money money2 = Money.wons(1000L);
        //when
        //then
        assertThat(money1).isEqualTo(money2);
    }

    @Test
    @DisplayName("금액이 다르면 다른 값이다")
    void money_not_equal_amount() {
        //given
        Money money1 = Money.wons(1000L);
        Money money2 = Money.wons(1001L);
        //when
        //then
        assertThat(money1).isNotEqualTo(money2);
    }
    
    @Test
    @DisplayName("금액은 null 이 될 수 없다")
    void money_is_not_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Money.wons(null))
                .isInstanceOf(InvalidDomainValueException.class)
                .hasMessage("금액은 null 이 될 수 없습니다.");
    }

    @Test
    @DisplayName("금액은 0보다 작을 수 없다")
    void money_is_not_less_than_0() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Money.wons(-1L))
                .isInstanceOf(InvalidDomainValueException.class)
                .hasMessage("금액은 0 보다 작을 수 없습니다.");
    }

    @Test
    @DisplayName("금액을 더한다")
    void add() {
        //given
        Money current = Money.wons(10L);
        Money added = Money.wons(5L);
        Money result = Money.wons(15L);
        //when
        Money sum = current.add(added);
        //then
        assertThat(sum).isEqualTo(result);
    }

    @Test
    @DisplayName("금액을 뺀다")
    void subtract() {
        //given
        Money current = Money.wons(15L);
        Money deducted = Money.wons(5L);
        Money result = Money.wons(10L);
        //when
        Money subtract = current.subtract(deducted);
        //then
        assertThat(subtract).isEqualTo(result);
    }

    @Test
    @DisplayName("차감된 금액이 0 이하일 수 없다")
    void subtract_is_not_less_than_0() {
        //given
        Money current = Money.wons(5L);
        Money deducted = Money.wons(10L);
        //when
        //then
        assertThatThrownBy(() -> current.subtract(deducted))
                .isInstanceOf(InvalidDomainValueException.class)
                .hasMessage("차감 금액이 현재 금액보다 클 수 없습니다.");
    }
}
