package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.category.domain.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductTest {

    @Test
    @DisplayName("상품을 생성한다")
    void create(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        Product product = Product.create("상품", "상품설명", category);
        //then
        assertThat(product)
                .extracting(
                        Product::getName, Product::getCategory, Product::getStatus, Product::getDescription, Product::getPublishedAt,
                        Product::getThumbnail, Product::getRating, Product::getReviewCount, Product::getDisplayPrice, Product::getOriginalPrice,
                        Product::getMaxDiscountRate)
                .containsExactly(
                        "상품", category, ProductStatus.PREPARING, "상품설명", null,
                        null, 0.0, 0L, null, null, null
                );

    }
}
