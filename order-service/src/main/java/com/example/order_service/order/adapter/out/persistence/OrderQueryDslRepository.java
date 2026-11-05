package com.example.order_service.order.adapter.out.persistence;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderSortType;
import com.example.order_service.order.domain.order.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

import static com.example.order_service.order.domain.order.QOrder.order;
import static com.example.order_service.order.domain.order.QOrderItem.orderItem;


@Slf4j
@Repository
public class OrderQueryDslRepository {

    public OrderQueryDslRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private final JPAQueryFactory queryFactory;

    public Page<Order> searchOrders(Long ordererId, OrderSearchCommand command) {
        Pageable pageable = command.getPageable();
        OrderSpecifier<?> sort = toOrderSpecifier(command.getSort());
        List<Order> result = queryFactory
                .selectFrom(order).distinct()
                .join(order.orderItems, orderItem)
                .where(
                        order.orderer.userId.eq(ordererId),
                        yearEq(command.getYear()),
                        productNameEq(command.getProductName()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sort)
                .fetch();

        Long totalElement = queryFactory.select(order.countDistinct())
                .from(order)
                .join(order.orderItems, orderItem)
                .where(
                        order.orderer.userId.eq(ordererId),
                        yearEq(command.getYear()),
                        productNameEq(command.getProductName()))
                .fetchOne();

        return new PageImpl<>(
                result,
                pageable,
                totalElement != null ? totalElement : 0L
        );
    }

    private BooleanExpression yearEq(Year year) {
        if (year == null) {
            return null;
        }

        LocalDateTime start = year.atDay(1).atStartOfDay();
        LocalDateTime end = year.plusYears(1).atDay(1).atStartOfDay();
        return order.createdAt.goe(start)
                .and(order.createdAt.lt(end));
    }

    private OrderSpecifier<?> toOrderSpecifier(OrderSortType orderSortType) {
        return switch (orderSortType) {
            case LATEST -> order.createdAt.desc();
            case OLDEST -> order.createdAt.asc();
            default -> order.createdAt.desc();
        };
    }

    private BooleanExpression productNameEq(String productName) {
        if (productName == null || productName.isEmpty()) {
            return null;
        }
        return orderItem.product.productName.contains(productName);
    }
}
