package com.example.order_service.common.domain.vo;

import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 금액 VO
 * 금액을 관리하는 Value Object
 *
 * @author 2026. 05. 23
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    private BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * 정적 팩토리 메서드
     * <p>
     * amount 금액의 Money 생성
     * </p>
     *
     * @param amount 금액
     * @return 금액 VO
     * @throws InvalidDomainValueException 도메인 계층 예외
     */
    public static Money wons(Long amount) {
        if (amount == null) {
            throw new InvalidDomainValueException("금액은 null 이 될 수 없습니다.");
        }
        if (amount < 0) {
            throw new InvalidDomainValueException("금액은 0 보다 작을 수 없습니다.");
        }
        if (amount == 0) {
            return ZERO;
        }
        return new Money(BigDecimal.valueOf(amount));
    }

    /**
     * added 금액 더하기
     *
     * @param added 더하는 금액
     * @return 더해진 금액
     */
    public Money add(Money added) {
        return new Money(amount.add(added.amount));
    }

    /**
     * deducted 금액 빼기
     *
     * @param deducted 빼는 금액
     * @return 빼진 금액
     * @throws InvalidDomainValueException 도메인 계층 예외
     */
    public Money subtract(Money deducted) {
        if (isLessThan(deducted)) {
            throw new InvalidDomainValueException("차감 금액이 현재 금액보다 클 수 없습니다.");
        }
        return new Money(amount.subtract(deducted.amount));
    }

    /**
     * multiplier 금액 곱하기
     *
     * @param multiplier 곱하는 금액
     * @return 곱해진 금액
     * @throws InvalidDomainValueException 도메인 계층 예외
     */
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

    /**
     * multiplier 금액 곱하기
     * <p>
     * 곱한 결과가 부동 소수점이라면 소수점을 버린 금액을 반환한다
     * </p>
     *
     * @param multiplier 곱할 금액
     * @return 곱해진 금액
     */
    public Money multiple(double multiplier) {
        if (multiplier < 0.0) {
            throw new InvalidDomainValueException("금액에 음수를 곱할 수 없습니다.");
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

    /**
     * multiplier 금액 곱하기
     * <p>
     * 곱한 결과가 부동 소수점이라면 소수점을 버린 금액을 반환한다
     * </p>
     *
     * @param multiplier 곱할 금액
     * @return 곱해진 금액
     */
    public Money multiple(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDomainValueException("금액에 음수를 곱할 수 없습니다.");
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

    /**
     * 금액 long 타입 반환
     * <p>
     * 금액을 long 타입으로 변환하여 반환한다
     * </p>
     *
     * @return long 타입 금액
     */
    public long longValue() {
        return amount.longValue();
    }

    /**
     * other 금액보다 작은지 판단
     * <p>
     * other 금액보다 작으면 true, 크면 false 반환
     * </p>
     *
     * @param other 비교할 금액
     * @return 비교 결과
     */
    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * other 금액보다 큰지 판단
     * <p>
     * other 금액보다 크면 true, 작으면 false 반환
     * </p>
     *
     * @param other 비교할 금액
     * @return 비교 결과
     */
    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    /**
     * other 금액과 자신중 작은 금액을 반환
     *
     * @param other 비교할 금액
     * @return 두 값중 작은 금액
     */
    public Money min(Money other) {
        return this.isLessThan(other) ? this : other;
    }

    /**
     * other 금액과 자신중 큰 금액을 반환
     *
     * @param other 비교할 금액
     * @return 두 값중 큰 금액
     */
    public Money max(Money other) {
        return this.isGreaterThan(other) ? this : other;
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
