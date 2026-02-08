package com.example.product_service.api.product.domain.repository;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.support.ExcludeInfraTest;
import com.example.product_service.support.fixture.builder.ProductTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
public class ProductRepositoryTest extends ExcludeInfraTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리와 이름으로 상품 목록을 조회한다")
    void findProductsByCondition_category_latest(){
        //given
        Category phone = saveCategory("핸드폰");
        Category food = saveCategory("식품");
        Product iphone = saveProduct(phone, ProductStatus.ON_SALE,"아이폰",  10000L, 12000L, 10, 4.3, 3.0);
        Product galaxy23 = saveProduct(phone, ProductStatus.ON_SALE,"갤럭시 23", 11000L, 12000L, 10, 4.0, 3.5);
        Product galaxy24 = saveProduct(phone, ProductStatus.ON_SALE,"갤럭시 24", 12000L, 13000L, 10, 2.0, 3.5);
        Product orange = saveProduct(food, ProductStatus.ON_SALE,"오렌지", 9000L, 12000L, 10, 3.9, 4.0);
        ProductSearchCondition condition = ProductSearchCondition.builder()
                .page(1)
                .size(10)
                .categoryId(phone.getId())
                .rating(4)
                .name("갤럭시")
                .build();
        //when
        Page<Product> result = productRepository.findProductsByCondition(condition);
        //then
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        assertThat(result.getContent())
                .extracting(Product::getId, Product::getName)
                .containsExactly(
                        tuple(galaxy23.getId(), galaxy23.getName())
                );
    }

    @Test
    @DisplayName("최신순으로 정렬된 상품 목록을 조회한다")
    void findProductsByCondition_with_sort(){
        //given
        Category phone = saveCategory("핸드폰");
        Product galaxy23 = saveProduct(phone, ProductStatus.ON_SALE,"갤럭시 23", 11000L, 12000L, 10, 4.0, 3.5);
        Product galaxy24 = saveProduct(phone, ProductStatus.ON_SALE,"갤럭시 24", 12000L, 13000L, 10, 4.5, 3.5);
        ProductSearchCondition condition = ProductSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("latest")
                .build();
        //when
        Page<Product> result = productRepository.findProductsByCondition(condition);
        //then
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(Product::getId, Product::getName)
                .containsExactly(
                        tuple(galaxy24.getId(), galaxy24.getName()),
                        tuple(galaxy23.getId(), galaxy23.getName())
                );
    }

    private Category saveCategory(String name) {
        return categoryRepository.save(Category.create(name, null, "http://image.jpg"));
    }

    private Product saveProduct(Category category, ProductStatus status, String name, Long lowest, Long original, Integer discountRate,
                                Double rating, Double score) {
        Product product = ProductTestBuilder.aProduct()
                .withId(null)
                .withStatus(status)
                .withCategory(category)
                .withName(name)
                .withImages(List.of("http://test.jpg"))
                .withPrice(lowest, original, discountRate)
                .withScore(rating, 200L, score)
                .build();
        return productRepository.save(product);
    }
}
