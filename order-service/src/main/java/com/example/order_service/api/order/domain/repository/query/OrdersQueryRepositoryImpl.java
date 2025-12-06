package com.example.order_service.api.order.domain.repository.query;

import com.example.order_service.api.order.domain.model.Orders;
import com.example.order_service.api.order.domain.model.QOrderItems;
import com.example.order_service.api.order.domain.model.QOrders;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.api.order.domain.model.QOrderItems.orderItems;
import static com.example.order_service.api.order.domain.model.QOrders.orders;

@Repository
@Slf4j
public class OrdersQueryRepositoryImpl implements OrdersQueryRepository{

    private final JPAQueryFactory queryFactory;
    QOrders qOrders = orders;
    QOrderItems qOrderItems = orderItems;

    public OrdersQueryRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Orders> findAllByParameter(Pageable pageable, Long userId, Integer year, String keyword) {

        List<Orders> content = queryFactory.select(orders)
                .from(orders)
                .join(orders.orderItems, orderItems).fetchJoin()
                .where(eqUserId(userId), eqYear(year), containsKeyword(keyword))
                .offset(pageable.getOffset())
                .orderBy(createOrderSpecifierForOrders(pageable))
                .limit(pageable.getPageSize())
                .distinct().fetch();
        Long totalCount = queryFactory.select(orders.id.countDistinct())
                .from(orders)
                .join(orders.orderItems, orderItems)
                .where(eqUserId(userId), eqYear(year), containsKeyword(keyword))
                .distinct().fetchOne();

        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression eqUserId(Long userId){
        if(userId == null){
            return null;
        }

        return orders.userId.eq(userId);
    }

    private BooleanExpression eqYear(Integer year){
        if(year == null){
            return null;
        }
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year + 1, 1, 1, 0, 0);
        return orders.createAt.between(start, end.minusNanos(1));
    }

    private BooleanExpression containsKeyword(String keyword) {
        if(keyword == null || keyword.isEmpty()){
            return null;
        }
        return orderItems.productName.contains(keyword);
    }

    private OrderSpecifier<?> createOrderSpecifierForOrders(Pageable pageable){
        if (pageable.getSort().isEmpty()){
            return orders.id.asc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        String sortProperty = order.getProperty();
        PathBuilder<Orders> pathBuilder = new PathBuilder<>(Orders.class, orders.getMetadata());
        return new OrderSpecifier<>(
                order.isAscending() ? Order.ASC : Order.DESC,
                pathBuilder.getComparable(sortProperty, String.class)
        );
    }
}
