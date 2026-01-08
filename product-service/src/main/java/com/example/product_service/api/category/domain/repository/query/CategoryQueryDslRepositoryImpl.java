package com.example.product_service.api.category.domain.repository.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import static com.example.product_service.api.category.domain.model.QCategory.*;

@Repository
public class CategoryQueryDslRepositoryImpl implements CategoryQueryDslRepository{

    private final JPAQueryFactory query;

    public CategoryQueryDslRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    // 형제중 동일한 이름이 존재하는지 조회
    @Override
    public boolean existsDuplicateName(Long parentId, String name) {
        return query
                .selectOne()
                .from(category)
                .where(
                        category.name.eq(name),
                        eqParentId(parentId))
                .fetchFirst() != null;
    }

    private BooleanExpression eqParentId(Long parentId) {
        if (parentId == null) {
            return category.parent.isNull();
        }
        return category.parent.id.eq(parentId);
    }
}
