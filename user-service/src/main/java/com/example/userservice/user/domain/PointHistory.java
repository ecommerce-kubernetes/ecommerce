package com.example.userservice.user.domain;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.entity.BaseEntity;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.user.domain.vo.PointCommandType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Entity
@Table(
        name = "point_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_point_history_reference_id", columnNames = {"reference_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseEntity {

    @Id
    private Long id;

    @Column(name = "reference_id")
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Money amount;

    @Enumerated(EnumType.STRING)
    private PointCommandType type;

    @Builder(access = AccessLevel.PRIVATE)
    private PointHistory(Long id, Long referenceId, User user, Money amount, PointCommandType type) {
        Assert.notNull(id, "포인트 기록 아이디는 필수이다.");
        Assert.notNull(referenceId, "포인트 기록 연관 아이디는 필수이다.");
        Assert.notNull(user, "포인트 기록 유저는 필수이다.");
        Assert.notNull(amount, "포인트 기록 금액은 필수이다.");
        Assert.notNull(type, "포인트 기록 타입은 필수이다.");

        this.id = id;
        this.referenceId = referenceId;
        this.user = user;
        this.amount = amount;
        this.type = type;
    }

    public static PointHistory createAddHistory(IdGenerator idGenerator, Long referenceId, User user, Money amount) {
        Long id = idGenerator.generate();

        return PointHistory.builder()
                .id(id)
                .referenceId(referenceId)
                .user(user)
                .amount(amount)
                .type(PointCommandType.ADD)
                .build();
    }

    public static PointHistory createDeductHistory(IdGenerator idGenerator, Long referenceId, User user, Money amount) {
        Long id = idGenerator.generate();

        return PointHistory.builder()
                .id(id)
                .referenceId(referenceId)
                .user(user)
                .amount(amount)
                .type(PointCommandType.DEDUCT)
                .build();
    }
}
