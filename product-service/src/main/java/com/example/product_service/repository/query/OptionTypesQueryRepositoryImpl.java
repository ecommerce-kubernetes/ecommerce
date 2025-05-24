package com.example.product_service.repository.query;

import com.example.product_service.dto.response.PageDto;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.QOptionTypes;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OptionTypesQueryRepositoryImpl implements OptionTypesQueryRepository{

    private final JPAQueryFactory queryFactory;
    QOptionTypes optionTypes = QOptionTypes.optionTypes;

    public OptionTypesQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<OptionTypes> findByNameOrAll(String query, Pageable pageable) {
        List<OptionTypes> content = queryFactory
                .select(optionTypes)
                .from(optionTypes)
                .where(containsName(query))
                .orderBy(buildOrders(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()).fetch();

        Long totalCount = queryFactory
                .select(optionTypes.id.countDistinct())
                .from(optionTypes)
                .where(containsName(query))
                .fetchOne();
        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression containsName(String name){
        if(name == null || name.isEmpty()){
            return null;
        }
        return optionTypes.name.contains(name);
    }

    private OrderSpecifier<?>[] buildOrders(Pageable pageable){
        PathBuilder<OptionTypes> path =
                new PathBuilder<>(OptionTypes.class, optionTypes.getMetadata().getName());

        return pageable.getSort().stream()
                .map(sortOrder -> {
                    String property = sortOrder.getProperty();
                    Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

                    Expression<? extends Comparable<?>> expr;
                    if("id".equals(property)){
                        expr = path.getNumber(property, Long.class);
                    } else {
                        expr = path.getString(property);
                    }
                    return new OrderSpecifier<>(direction, expr);
                })
                .toArray(OrderSpecifier[]::new);
    }
}
