package com.example.product_service.api.category.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.support.ExcludeInfraTest;
import com.example.product_service.entity.Product;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class CategoryServiceTest extends ExcludeInfraTest {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("최상위 카테고리를 생성한다")
    void saveCategory_when_root(){
        //given
        //when
        CategoryResponse result = categoryService.saveCategory("카테고리", null, "http://image.jpg");
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth, CategoryResponse::getImageUrl)
                .containsExactly("카테고리", null, 1, "http://image.jpg");

        Category category = categoryRepository.findById(result.getId()).get();
        assertThat(category.getPath()).isEqualTo(String.valueOf(result.getId()));
    }

    @Test
    @DisplayName("자식 카테고리를 생성한다")
    void saveCategory_when_child(){
        //given
        Category parent = Category.create("부모", null, "http://parent.jpg");
        Category savedParent = categoryRepository.save(parent);
        savedParent.generatePath();
        //when
        CategoryResponse result = categoryService.saveCategory("자식", savedParent.getId(), "http://child.jpg");
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth, CategoryResponse::getImageUrl)
                .containsExactly("자식", savedParent.getId(), 2, "http://child.jpg");

        Category category = categoryRepository.findById(result.getId()).get();
        assertThat(category.getPath()).isEqualTo(savedParent.getPath() + "/" + result.getId());
    }

    @Test
    @DisplayName("자식 카테고리를 생성할때 부모 카테고리를 찾을 수 없으면 예외를 던진다")
    void saveCategory_when_notFound_parent(){
        //given
        //when
        //then
        assertThatThrownBy(() -> categoryService.saveCategory("자식", 999L, "http://child.jpg"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("자식 카테고리를 생성할때 부모 카테고리에 속한 상품이 존재하는 경우 예외를 던진다")
    void saveCategory_when_product_in_parentCategory(){
        //given
        Category category = Category.create("카테고리", null, "http://parent.jpg");
        Category savedCategory = categoryRepository.save(category);
        Product product = Product.create("상품", "상품 설명", savedCategory);
        productRepository.save(product);
        //when
        //then
        assertThatThrownBy(() -> categoryService.saveCategory("자식", savedCategory.getId(), "http://child.jpg"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.HAS_PRODUCT);
    }
}
