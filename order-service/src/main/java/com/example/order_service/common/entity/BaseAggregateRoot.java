package com.example.order_service.common.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@MappedSuperclass
public abstract class BaseAggregateRoot extends BaseEntity{

    @Transient
    private transient final List<Object> domainEvents = new ArrayList<>();

    protected <T> T registerEvent(T event) {
        Assert.notNull(event, "도메인 이벤트 생성시 이벤트는 필수이다.");
        this.domainEvents.add(event);
        return event;
    }

    @DomainEvents
    protected Collection<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
