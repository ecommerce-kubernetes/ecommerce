package com.example.product_service.dto.response.product;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.example.product_service.dto.response.product.QProductSummaryDto is a Querydsl Projection type for ProductSummaryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QProductSummaryDto extends ConstructorExpression<ProductSummaryDto> {

    private static final long serialVersionUID = -649336827L;

    public QProductSummaryDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<String> description, com.querydsl.core.types.Expression<String> thumbnailUrl, com.querydsl.core.types.Expression<Integer> originPrice, com.querydsl.core.types.Expression<Integer> discountPrice, com.querydsl.core.types.Expression<Integer> discountValue) {
        super(ProductSummaryDto.class, new Class<?>[]{long.class, String.class, String.class, String.class, int.class, int.class, int.class}, id, name, description, thumbnailUrl, originPrice, discountPrice, discountValue);
    }

    public QProductSummaryDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<String> description, com.querydsl.core.types.Expression<String> thumbnailUrl, com.querydsl.core.types.Expression<Integer> originPrice) {
        super(ProductSummaryDto.class, new Class<?>[]{long.class, String.class, String.class, String.class, int.class}, id, name, description, thumbnailUrl, originPrice);
    }

}

