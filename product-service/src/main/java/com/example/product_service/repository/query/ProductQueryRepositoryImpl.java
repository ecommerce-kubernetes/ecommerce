package com.example.product_service.repository.query;

import com.example.product_service.entity.Products;
//import com.querydsl.jpa.impl.JPAQueryFactory;
import com.example.product_service.entity.QProducts;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

    private final JPAQueryFactory queryFactory;
    QProducts products = QProducts.products;

    public ProductQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Products> findAllByParameter(String name, Long categoryId, Pageable pageable) {
        List<Products> content = queryFactory
                .select(products)
                .from(products)
                .where(containsName(name), eqCategoryId(categoryId))
                .orderBy(createOrderSpecifierForProducts(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()).fetch();

        Long totalCount = queryFactory
                .select(Wildcard.count)
                .from(products)
                .where(containsName(name), eqCategoryId(categoryId))
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression containsName(String name){
        if(name == null || name.isEmpty()){
            return null;
        }
        return products.name.contains(name);
    }

    private BooleanExpression eqCategoryId(Long categoryId){
        if(categoryId == null){
            return null;
        }
        return products.category.id.eq(categoryId);
    }

    private OrderSpecifier<?> createOrderSpecifierForProducts(Pageable pageable){
        if (pageable.getSort().isEmpty()){
            return products.id.asc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        String sortProperty = order.getProperty();

        if ("categoryId".equals(sortProperty)) {
            // products.category.id 로 정렬하기 위해 QCategories의 별칭을 활용할 수 있습니다.
            return new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    products.category.id
            );
        }
        PathBuilder<Products> pathBuilder = new PathBuilder<>(Products.class, products.getMetadata());
        return new OrderSpecifier<>(
                order.isAscending() ? Order.ASC : Order.DESC,
                pathBuilder.getComparable(sortProperty, String.class)
        );
    }
}
