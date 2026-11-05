package com.example.userservice.common.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    private BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    @JsonCreator
    public static Money wons(Long amount) {
        if (amount == null) {
            throw new IllegalArgumentException("금액은 null 이 될 수 없습니다.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 보다 작을 수 없습니다.");
        }
        if (amount == 0) {
            return ZERO;
        }
        return new Money(BigDecimal.valueOf(amount));
    }

    public Money add(Money added) {
        return new Money(amount.add(added.amount));
    }

    public Money subtract(Money deducted) {
        if (isLessThan(deducted)) {
            throw new IllegalArgumentException("차감 금액이 현재 금액보다 클 수 없습니다.");
        }
        return new Money(amount.subtract(deducted.amount));
    }

    public Money multiple(long multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("금액에 음수를 곱할 수 없습니다.");
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
            throw new IllegalArgumentException("금액에 음수를 곱할 수 없습니다.");
        }
        if (multiplier == 0.0) {
            return ZERO;
        }
        if (multiplier == 1.0) {
            return this;
        }
        BigDecimal multipliedAmount = amount.multiply(BigDecimal.valueOf(multiplier));
        multipliedAmount = multipliedAmount.setScale(0, RoundingMode.DOWN);
        return new Money(multipliedAmount);
    }

    public Money multiple(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액에 음수를 곱할 수 없습니다.");
        }

        if (multiplier.compareTo(BigDecimal.ZERO) == 0) {
            return ZERO;
        }

        if (multiplier.compareTo(BigDecimal.ONE) == 0) {
            return this;
        }

        BigDecimal multipliedAmount = amount.multiply(multiplier);
        multipliedAmount = multipliedAmount.setScale(0, RoundingMode.DOWN);
        return new Money(multipliedAmount);
    }

    @JsonValue
    public long longValue() {
        return amount.longValue();
    }

    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    public static Money min(Money a, Money b) {
        return a.isLessThan(b) ? a : b;
    }

    public static Money max(Money a, Money b){
        return a.isGreaterThan(b) ? a : b;
    }

    public Money truncateToTens() {
        long truncated = (this.amount.longValue() / 10) * 10;
        return Money.wons(truncated);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;

        // [NOTE] 10.0 와 10.00 은 같은 금액
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
