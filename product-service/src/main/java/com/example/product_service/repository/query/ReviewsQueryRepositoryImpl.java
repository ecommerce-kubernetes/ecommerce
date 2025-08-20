package com.example.product_service.repository.query;

import com.example.product_service.entity.*;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ReviewsQueryRepositoryImpl implements ReviewsQueryRepository{

    private final JPAQueryFactory queryFactory;
    QReviews reviews = QReviews.reviews;
    QProductVariants productVariants = QProductVariants.productVariants;
    QProductVariantOptions productVariantOptions = QProductVariantOptions.productVariantOptions;
    QProducts products = QProducts.products;
    public ReviewsQueryRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Reviews> findAllByProductId(Long productId, Pageable pageable) {
        List<Reviews> content = queryFactory.selectFrom(reviews)
                .distinct()
                .join(reviews.productVariant, productVariants).fetchJoin()
                .join(productVariants.product, products).fetchJoin()
                .where(reviews.productVariant.product.id.eq(productId))
                .orderBy(createOrderSpecifierForReviews(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(reviews.id.countDistinct())
                .from(reviews)
                .where(reviews.productVariant.product.id.eq(productId))
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount);
    }

    private OrderSpecifier<?> createOrderSpecifierForReviews(Pageable pageable){
        if(pageable.getSort().isEmpty()){
            return reviews.id.asc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        String sortProperty = order.getProperty();

        PathBuilder<Reviews> pathBuilder = new PathBuilder<>(Reviews.class, reviews.getMetadata());
        return new OrderSpecifier<>(
                order.isAscending() ? Order.ASC : Order.DESC,
                pathBuilder.getComparable(sortProperty, String.class)
        );
    }
}
