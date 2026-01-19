package com.example.product_service.api.product.domain.repository.query;

import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.product_service.api.category.domain.model.QCategory.category;
import static com.example.product_service.api.product.domain.model.QProduct.product;

@Repository
public class ProductQueryDslRepositoryImpl implements ProductQueryDslRepository{

    private final JPAQueryFactory factory;

    public ProductQueryDslRepositoryImpl(EntityManager em) {
        this.factory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Product> findProductsByCondition(ProductSearchCondition condition) {
        Pageable pageable = condition.getPageable();
        OrderSpecifier<?> sortOrder = ProductQueryMapper.toOrderSpecifier(condition.getSort());
        List<Product> result = factory.select(product)
                .from(product)
                .join(product.category, category).fetchJoin()
                .where(eqCategory(condition.getCategoryId()),
                        containName(condition.getName()),
                        filterRating(condition.getRating()),
                        product.status.eq(ProductStatus.ON_SALE))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortOrder)
                .fetch();

        Long totalElement = factory.select(product.countDistinct())
                .from(product)
                .join(product.category, category)
                .where(eqCategory(condition.getCategoryId()),
                        containName(condition.getName()),
                        filterRating(condition.getRating()),
                        product.status.eq(ProductStatus.ON_SALE))
                .fetchOne();

        return new PageImpl<>(
                result,
                pageable,
                totalElement != null ? totalElement : 0L
        );
    }

    private BooleanExpression eqCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return category.id.eq(categoryId);
    }

    private BooleanExpression containName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        return product.name.contains(name);
    }

    private BooleanExpression filterRating(Integer rating) {
        if (rating == null) {
            return null;
        }

        return product.rating.goe(rating).and(product.rating.lt(rating + 1));
    }
}
