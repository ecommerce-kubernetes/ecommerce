package com.example.product_service.repository.query;

import com.example.product_service.dto.response.product.*;
import com.example.product_service.entity.*;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Slf4j
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

    private final JPAQueryFactory queryFactory;
    QProducts products = QProducts.products;
    QProductImages productImages = QProductImages.productImages;
    QProductVariants productVariants = QProductVariants.productVariants;
    QReviews reviews = QReviews.reviews;
    QCategories categories = QCategories.categories;
    public ProductQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDto> findAllByProductSummaryProjection(String name, List<Long> categoryIds, Integer rating,
                                                                     Pageable pageable) {
        /*
        상품 조회 로직 => 상품 기본 정보와 상품의 Variants 중 할인된 가격이 가장 작은 variant 의 가격, 할인가격, 할인율을 조회

        예상 쿼리
        SELECT p.id, p.name, p.description, img.imageUrl, pv.price, (pv.price * (100 - pv.discount_value) / 100), pv.discount_value
        FROM products p
        LEFT JOIN categories c p.category_id = c.id
        LEFT JOIN products_images img ON img.product_id = p.id AND img.sort_order = 0
        LEFT JOIN products_variants pv ON pv.product_id = p.id
            AND ( pv.price * (100 - pv.discount_value) / 100 ) =
                                ( SELECT MIN( pv2.price * ( 100 - pv2.discount_value ) / 100 )
                                  FROM product_variants pv2
                                  WHERE pv2.product_id = p.id )
        --------------------------------------------------------------------------------------------------------------
        [LEFT JOIN products_images img ON img.product_id = p.id AND img.sort_order = 0]
        => 상품 이미지 중 sort_order 가 0 인 이미지와 상품 JOIN

        [LEFT JOIN products_variants pv ON pv.product_id = p.id
          AND ( pv.price * (100 - pv.discount_value) / 100 ) =
                              ( SELECT MIN( pv2.price * ( 100 - pv2.discount_value ) / 100 )
                                FROM product_variants pv2
                                WHERE pv2.product_id = p.id )]
        => 상품 Variant 와 상품을 Join 하는데 왼쪽항 ( 해당 상품의 모든 Variant 의 할인 가격을 계산 ) 중 오른쪽 항 ( Variant 중 할인 가격이 가장 작은 Variant ) 과 동일한 Variant 를 JOIN
         */

        // QueryDsl 에서의 연산 메서드(subtract(), multiply(), divide()) 는 NumberExpression<?> 타입의 표현식을 사용  따라서 100과 같은 원시타입은 사용할 수 없으므로
        // 해당 Expression 노드로 래핑
        NumberExpression<Integer> hundred = Expressions.numberTemplate(Integer.class, "{0}", 100);
        // pv.price * (100 - pv.discount_value) / 100 해당 계산식을 노드로 생성
        NumberExpression<Integer> discountPriceExpr =
                productVariants.price.multiply(hundred.subtract(productVariants.discountValue)).divide(hundred);

        NumberTemplate<Double> avgRatingExpr = Expressions.numberTemplate(Double.class, "( SELECT AVG(r.rating) " +
                "FROM Reviews r " +
                "WHERE r.product.id = {0} )", products.id);
        NumberTemplate<Integer> reviewCountExpr = Expressions.numberTemplate(Integer.class, "( SELECT COUNT(*) " +
                "FROM Reviews r " +
                "WHERE r.product.id = {0} )", products.id);

        BooleanExpression ratingPredicate =
                (rating != null && rating > 0) ? avgRatingExpr.goe(rating.doubleValue()) : null;

        List<ProductSummaryDto> content = queryFactory.selectDistinct(
                        new QProductSummaryDto(
                                products.id,
                                products.name,
                                products.description,
                                productImages.imageUrl,
                                categories.name,
                                products.createAt,
                                avgRatingExpr,
                                reviewCountExpr,
                                productVariants.price,
                                discountPriceExpr,
                                productVariants.discountValue))
                .from(products)
                .leftJoin(products.category, categories)
                .leftJoin(products.images, productImages).on(productImages.sortOrder.eq(0))
                .leftJoin(products.productVariants, productVariants)
                        .on(discountPriceExpr.eq(JPAExpressions.select(discountPriceExpr.min()).from(productVariants)
                                .where(productVariants.product.eq(products))))
                .where(containsName(name), inCategoryIds(categoryIds), ratingPredicate)
                .orderBy(createOrderSpecifierForProducts(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory.select(products.id.countDistinct())
                .from(products)
                .where(containsName(name), inCategoryIds(categoryIds), ratingPredicate)
                .fetchOne();
        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression containsName(String name){
        if(name == null || name.isEmpty()){
            return null;
        }
        return products.name.contains(name);
    }

    private BooleanExpression inCategoryIds(List<Long> categoryIds){
        if(categoryIds == null || categoryIds.isEmpty()){
            return null;
        }
        return products.category.id.in(categoryIds);
    }

    private OrderSpecifier<?> createOrderSpecifierForProducts(Pageable pageable){
        if (pageable.getSort().isEmpty()){
            return products.id.asc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        String sortProperty = order.getProperty();

        if ("categoryId".equals(sortProperty)) {
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
