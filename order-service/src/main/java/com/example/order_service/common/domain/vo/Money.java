package com.example.order_service.common.domain.vo;

import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {
    // 가격이 0 인 상수
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    private BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money wons(Long amount) {
        // 금액은 null 이 될 수 없음
        if (amount == null) {
            throw new InvalidDomainValueException("금액은 null 이 될 수 없습니다.");
        }
        // 금액은 0 보다 작을 수 없음
        if (amount < 0) {
            throw new InvalidDomainValueException("금액은 0 보다 작을 수 없습니다.");
        }
        // 금액이 0 이면 ZERO 인스턴스 반환
        if (amount == 0) {
            return ZERO;
        }
        return new Money(BigDecimal.valueOf(amount));
    }

    public Money add(Money added) {
        return new Money(amount.add(added.amount));
    }

    public Money subtract(Money deducted) {
        if (isLessThan(deducted)){
            throw new InvalidDomainValueException("차감 금액이 현재 금액보다 클 수 없습니다.");
        }
        return new Money(amount.subtract(deducted.amount));
    }

    public Money multiple(long multiplier) {
        if (multiplier < 0) {
            throw new InvalidDomainValueException("금액에 음수를 곱할 수 없습니다.");
        }
        if (multiplier == 0) {
            return ZERO;
        }
        if (multiplier == 1) {
            return this;
        }
        return new Money(amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    public Money multiple(double multiplier) {
        if (multiplier < 0.0) {
            throw new InvalidDomainValueException("금액에 음수를 곱할 수 없습니다.");
        }
        if (multiplier == 0.0){
            return ZERO;
        }
        if (multiplier == 1.0){
            return this;
        }
        // 금액 곱
        BigDecimal multipliedAmount = amount.multiply(BigDecimal.valueOf(multiplier));
        // 소수점 버림
        multipliedAmount = multipliedAmount.setScale(0, RoundingMode.DOWN);
        return new Money(multipliedAmount);
    }

    public long longValue() {
        return amount.longValue();
    }

    public boolean isLessThan(Money other) {
        // other 보다 금액이 적으면 true
        return amount.compareTo(other.amount) < 0;
    }

    @Override
    public boolean equals(Object o) {
        // 같은 객체이면 true
        if(this == o) return true;
        // 다른 객체이면 false
        if (!(o instanceof Money money)) return false;
        // 가격이 같으면 같은 객체
        // 10.0 과 10.00은 같은 값 객체
        return this.amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return this.amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.stripTrailingZeros().toPlainString() + "원";
    }
}
