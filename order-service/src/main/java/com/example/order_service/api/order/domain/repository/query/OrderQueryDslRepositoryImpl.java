package com.example.order_service.api.order.domain.repository.query;

import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.api.order.domain.model.QOrder.order;
import static com.example.order_service.api.order.domain.model.QOrderItem.orderItem;

@Repository
public class OrderQueryDslRepositoryImpl implements OrderQueryDslRepository{

    public OrderQueryDslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> findByUserIdAndCondition(Long userId, OrderSearchCondition condition) {
        Pageable pageable = condition.getPageable();
        OrderSpecifier<?> sortOrder = OrderQueryMapper.toOrderSpecifier(condition.getSort());
        List<Order> result = queryFactory.select(order).distinct()
                .from(order)
                .join(order.orderItems, orderItem)
                .where(
                        yearEq(condition.getYear()),
                        order.orderer.userId.eq(userId),
                        productNameEq(condition.getProductName())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortOrder, order.id.desc())
                .fetch();

        Long totalElement = queryFactory.select(order.countDistinct())
                .from(order)
                .leftJoin(order.orderItems, orderItem)
                .where(
                        yearEq(condition.getYear()),
                        order.orderer.userId.eq(userId),
                        productNameEq(condition.getProductName())
                )
                .fetchOne();

        return new PageImpl<>(
                result,
                pageable,
                totalElement != null ? totalElement : 0L
        );
    }

    private BooleanExpression yearEq(String yearString) {
        if (yearString == null || yearString.isEmpty()){
            return null;
        }

        int year = Integer.parseInt(yearString);
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year + 1, 1, 1, 0, 0);

        return order.createdAt.goe(start)
                .and(order.createdAt.lt(end));
    }

    private BooleanExpression productNameEq(String productName) {
        if (productName == null || productName.isEmpty()) {
            return null;
        }
        return orderItem.orderedProduct.productName.contains(productName);
    }
}
