package com.example.product_service.repository.query;

import com.example.product_service.entity.ProductSummary;
import com.example.product_service.entity.QProductSummary;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductSummaryQueryRepositoryImpl implements ProductSummaryQueryRepository{
    private final JPAQueryFactory queryFactory;
    public ProductSummaryQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }
    QProductSummary productSummary = QProductSummary.productSummary;
    @Override
    public Page<ProductSummary> findAllProductSummary(String name, List<Long> categoryIds, Integer rating, Pageable pageable) {
        List<ProductSummary> content = queryFactory.select(productSummary)
                .from(productSummary).fetch();
        return new PageImpl<>(content, pageable, 1);
    }
}
