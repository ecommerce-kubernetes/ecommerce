package com.example.userservice.user.domain;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;

public class PointHistoryFixtureBuilder {

    private static final IdGenerator ID_GENERATOR = new TsidGenerator();

    private Long referenceId = 1L;
    private User user;
    private Money amount = Money.wons(1000L);

    public static PointHistoryFixtureBuilder given() {
        return new PointHistoryFixtureBuilder();
    }

    public PointHistoryFixtureBuilder withReferenceId(Long referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public PointHistoryFixtureBuilder withUser(User user) {
        this.user = user;
        return this;
    }

    public PointHistoryFixtureBuilder withAmount(Money amount) {
        this.amount = amount;
        return this;
    }

    public PointHistory buildAddHistory() {
        return PointHistory.createAddHistory(ID_GENERATOR.generate(), referenceId, user, amount);
    }

    public PointHistory buildDeductHistory() {
        return PointHistory.createDeductHistory(ID_GENERATOR.generate(), referenceId, user, amount);
    }
}
