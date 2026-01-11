package com.example.product_service.repository.query;

import com.example.product_service.entity.ProductSummary;
import com.example.product_service.entity.QProductSummary;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ProductSummaryQueryRepositoryImpl implements ProductSummaryQueryRepository{
    private final JPAQueryFactory queryFactory;
    public ProductSummaryQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }
    QProductSummary productSummary = QProductSummary.productSummary;

    @Override
    public Page<ProductSummary> findAllProductSummary(String name, List<Long> categoryIds, Integer rating, Pageable pageable) {
        List<ProductSummary> content = queryFactory.select(productSummary)
                .from(productSummary)
                .where(containsName(name), inCategoryIds(categoryIds), goeRating(rating))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(createOrderSpecifier(pageable))
                .fetch();

        Long totalCount = queryFactory.select(productSummary.id.countDistinct())
                .from(productSummary)
                .where(containsName(name), inCategoryIds(categoryIds), goeRating(rating))
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public Page<ProductSummary> findPopularProductSummary(List<Long> categoryIds, double C, int m, Pageable pageable) {

        NumberExpression<Double> v = Expressions.numberTemplate(Double.class, "COALESCE({0}, 0)", productSummary.reviewCount);
        NumberExpression<Double> R = Expressions.numberTemplate(Double.class, "COALESCE({0}, 0.0)", productSummary.avgRating);

        NumberExpression<Double> mExpr = Expressions.numberTemplate(Double.class, "{0}", (double) m);
        NumberExpression<Double> CExpr = Expressions.numberTemplate(Double.class, "{0}", C);

        NumberExpression<Double> denom = v.add(mExpr);

        // 베이시안 = (v/(v+m)) * R + (m/(v+m)) * C
        NumberExpression<Double> bayesExpr =
                v.divide(denom).multiply(R)
                        .add(mExpr.divide(denom).multiply(CExpr));

        List<ProductSummary> content = queryFactory
                .select(productSummary)
                .from(productSummary)
                .where(inCategoryIds(categoryIds), productSummary.reviewCount.gt(0))
                .orderBy(bayesExpr.desc(), productSummary.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(productSummary.id.countDistinct())
                .from(productSummary)
                .where(inCategoryIds(categoryIds), productSummary.reviewCount.gt(0))
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression containsName(String name){
        if(name == null || name.isEmpty()){
            return null;
        }
        return productSummary.name.contains(name);
    }

    private BooleanExpression inCategoryIds(List<Long> categoryIds){
        if(categoryIds == null || categoryIds.isEmpty()){
            return null;
        }
        return productSummary.categoryId.in(categoryIds);
    }

    private BooleanExpression goeRating(Integer rating){
        if(rating == null){
            return null;
        }
        return productSummary.avgRating.goe(rating);
    }

    private OrderSpecifier<?>[] createOrderSpecifier(Pageable pageable){
        PathBuilder<? extends ProductSummary> pathBuilder
                = new PathBuilder<>(productSummary.getType(), productSummary.getMetadata());
        return pageable.getSort().stream()
                .map(order -> {
                    Order dir = order.isAscending() ? Order.ASC : Order.DESC;
                    String prop = order.getProperty();

                    return new OrderSpecifier<>(dir, pathBuilder.getComparable(prop, Comparable.class));
                })
                .toArray(OrderSpecifier[]::new);
    }

}
