package com.example.product_service.api.category.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.support.ExcludeInfraTest;
import com.example.product_service.entity.Product;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

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
    @DisplayName("부모 카테고리에 더이상 자식 카테고리를 생성할 수 없는 경우 예외를 던짐")
    void saveCategory_when_exceed_max_depth(){
        //given
        Category parent = exceedDepthDCategory();
        Category savedParent = categoryRepository.save(parent);
        //when
        //then
        assertThatThrownBy(() -> categoryService.saveCategory("자식", savedParent.getId(), "http://image.jpg"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.EXCEED_MAX_DEPTH);
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

    @Test
    @DisplayName("형제 카테고리에 동일한 이름이 존재하는 경우 예외를 던진다[최상위 카테고리 생성시]")
    void saveCategory_when_duplicate_name_siblings_root(){
        //given
        Category category = Category.create("동일한 이름", null, "http://image.jpg");
        categoryRepository.save(category);
        //when
        //then
        assertThatThrownBy(() -> categoryService.saveCategory("동일한 이름", null, "http://image.jpg"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
    }

    @Test
    @DisplayName("형제 카테고리에 동일한 이름이 존재하는 경우 예외를 던진다[자식 카테고리 생성시]")
    void saveCategory_when_duplicate_name_siblings_child(){
        //given
        Category parent = Category.create("부모", null, "http://parent.jpg");
        Category.create("동일한 이름", parent, "http://child.jpg");
        Category savedParent = categoryRepository.save(parent);
        //when
        //then
        assertThatThrownBy(() -> categoryService.saveCategory("동일한 이름", savedParent.getId(), "http://child.jpg"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
    }

    @Test
    @DisplayName("카테고리를 조회한다")
    void getCategory(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        Category savedCategory = categoryRepository.save(category);
        //when
        CategoryResponse result = categoryService.getCategory(savedCategory.getId());
        //then
        assertThat(result.getId()).isEqualTo(savedCategory.getId());
        assertThat(result)
                .extracting(CategoryResponse::getName, CategoryResponse::getDepth, CategoryResponse::getParentId, CategoryResponse::getImageUrl)
                .containsExactly("카테고리", 1, null, "http://image.jpg");
    }

    @Test
    @DisplayName("카테고리를 조회할때 해당 카테고리를 찾을 수 없는 경우 예외를 던진다")
    void getCategory_when_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> categoryService.getCategory(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("전체 카테고리 트리구조를 조회한다")
    void getTree(){
        //given
        //depth 1
        Category electronics = Category.create("전자기기", null, "http://image.jpg");
        Category food = Category.create("식품", null, "http://image.jpg");
        categoryRepository.saveAll(List.of(electronics, food));

        //depth 2
        Category laptop = Category.create("노트북", electronics, "http://image.jpg");
        Category mobile = Category.create("핸드폰", electronics, "http://image.jpg");
        categoryRepository.saveAll(List.of(laptop, mobile));

        //depth 3
        Category macbook = Category.create("맥북", laptop, "http://image.jpg");
        Category gram = Category.create("그램", laptop, "http://image.jpg");
        categoryRepository.saveAll(List.of(macbook, gram));
        //when
        List<CategoryTreeResponse> result = categoryService.getTree();
        //then
        //depth 1 검증
        assertThat(result).hasSize(2)
                        .extracting(CategoryTreeResponse::getName, CategoryTreeResponse::getDepth)
                                .containsExactlyInAnyOrder(
                                        tuple("전자기기", 1),
                                        tuple("식품",1)
                                );

        CategoryTreeResponse electronicsResponse = findNodeByName(result, "전자기기");

        //depth 2 검증
        assertThat(electronicsResponse.getChildren()).hasSize(2)
                .extracting(CategoryTreeResponse::getName, CategoryTreeResponse::getDepth)
                .containsExactlyInAnyOrder(
                        tuple("노트북", 2),
                        tuple("핸드폰",2)
                );

        CategoryTreeResponse laptopResponse = findNodeByName(electronicsResponse.getChildren(), "노트북");

        //depth 3 검증
        assertThat(laptopResponse.getChildren()).hasSize(2)
                .extracting("name", "depth")
                .containsExactlyInAnyOrder(
                        tuple("맥북", 3),
                        tuple("그램", 3)
                );
    }

    @Test
    @DisplayName("카테고리 네비게이션을 조회한다")
    void getNavigation() {
        //given
        // parent
        Category electronics = Category.create("전자기기", null, "http://image.jpg");
        categoryRepository.save(electronics);
        electronics.generatePath();

        // target
        Category laptop = Category.create("노트북", electronics, "http://image.jpg");
        Category mobile = Category.create("핸드폰", electronics, "http://image.jpg");
        categoryRepository.saveAll(List.of(laptop, mobile));
        laptop.generatePath();
        mobile.generatePath();

        // child
        Category macbook = Category.create("맥북", laptop, "http://image.jpg");
        Category gram = Category.create("그램", laptop, "http://image.jpg");
        categoryRepository.saveAll(List.of(macbook, gram));
        macbook.generatePath();
        gram.generatePath();
        //when
        CategoryNavigationResponse result = categoryService.getNavigation(laptop.getId());
        //then
        // current 검증
        assertThat(result.getCurrent())
                .extracting(CategoryResponse::getName, CategoryResponse::getDepth)
                .containsExactly("노트북", 2);
        // path 검증
        assertThat(result.getAncestors()).hasSize(2)
                .extracting(CategoryResponse::getName, CategoryResponse::getDepth)
                .containsExactly(
                        tuple("전자기기", 1),
                        tuple("노트북", 2)
                );
        // child 검증
        assertThat(result.getChildren())
                .extracting(CategoryResponse::getName, CategoryResponse::getDepth)
                .containsExactlyInAnyOrder(
                        tuple("맥북", 3),
                        tuple("그램", 3)
                );
    }

    @Test
    @DisplayName("카테고리를 수정한다")
    void updateCategory() {
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        Category savedCategory = categoryRepository.save(category);
        //when
        CategoryResponse result = categoryService
                .updateCategory(savedCategory.getId(), "새 카테고리", "http://category.jpg");
        //then
        assertThat(result)
                .extracting(CategoryResponse::getId, CategoryResponse::getName, CategoryResponse::getImageUrl,
                        CategoryResponse::getDepth)
                .containsExactly(category.getId(), "새 카테고리", "http://category.jpg", 1);
    }

    @Test
    @DisplayName("카테고리를 수정할때 형제 카테고리에 같은 이름의 카테고리가 존재하면 예외를 던진다[최상위 카테고리 변경시]")
    void updateCategory_when_duplicate_name_root() {
        //given
        Category electronics = Category.create("전자기기", null, "http://image.jpg");
        Category food = Category.create("식품", null, "http://image.jpg");
        categoryRepository.saveAll(List.of(electronics, food));
        //when
        //then
        assertThatThrownBy(() -> categoryService.updateCategory(food.getId(), "전자기기", "http://image.jpg"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
    }

    @Test
    @DisplayName("카테고리를 수정할때 형제 카테고리에 같은 이름의 카테고리가 존재하면 예외를 던진다[자식 카테고리 변경시]")
    void updateCategory_when_duplicate_name_child() {
        //given
        Category electronics = Category.create("전자기기", null, "http//image.jpg");
        Category laptop = Category.create("노트북", electronics, "http://image.jpg");
        Category cellphone = Category.create("핸드폰", electronics, "http://image.jpg");
        categoryRepository.saveAll(List.of(electronics, laptop, cellphone));
        //when
        //then
        assertThatThrownBy(() -> categoryService.updateCategory(laptop.getId(), "핸드폰", "http://image.jpg"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
    }

    @Test
    @DisplayName("카테고리를 최상위 루트로 변경")
    void moveParent_move_root() {
        //given
        Category electronics = Category.create("전자기기", null, "http://image.jpg");
        Category savedElectronics = categoryRepository.save(electronics);
        savedElectronics.generatePath();
        Category cellphone = Category.create("핸드폰", savedElectronics, "http://image.jpg");
        Category savedCellphone = categoryRepository.save(cellphone);
        savedCellphone.generatePath();
        //when
        CategoryResponse result = categoryService.moveParent(savedCellphone.getId(), null, true);
        //then
        //depth 변경 확인
        assertThat(result)
                .extracting(CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth)
                .containsExactly("핸드폰", null, 1);
        //path 변경 확인
        Category find = categoryRepository.findById(savedCellphone.getId()).get();
        assertThat(find.getPath()).isEqualTo(String.valueOf(savedCellphone.getId()));
    }

    @Test
    @DisplayName("카테고리의 부모를 변경")
    void moveParent_move_child() {
        //given
        Category electronics = Category.create("전자기기", null, "http://image.jpg");
        Category savedElectronics = categoryRepository.save(electronics);
        savedElectronics.generatePath();
        Category food = Category.create("식품", null, "http://image.jpg");
        Category savedFood = categoryRepository.save(food);
        savedFood.generatePath();
        Category cellphone = Category.create("핸드폰", savedElectronics, "http://image.jpg");
        Category savedCellphone = categoryRepository.save(cellphone);
        savedCellphone.generatePath();
        //when
        CategoryResponse result = categoryService.moveParent(savedCellphone.getId(), savedFood.getId(), false);
        //then
        assertThat(result)
                .extracting(CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth)
                .containsExactly("핸드폰", savedFood.getId(), savedFood.getId() + "/" + savedCellphone.getId());
    }

    @Test
    @DisplayName("")
    void moveParent_duplicateName() {
        //given
        Category electronics = Category.create("전자기기", null, "http://image.jpg");
        Category savedElectronics = categoryRepository.save(electronics);
        savedElectronics.generatePath();
        Category food = Category.create("식품", null, "http://image.jpg");
        Category savedFood = categoryRepository.save(food);
        savedFood.generatePath();
        Category existCategory = Category.create("동일한 이름", savedElectronics, "http://image.jpg");
        Category savedExistCategory = categoryRepository.save(existCategory);
        savedExistCategory.generatePath();
        Category targetCategory = Category.create("동일한 이름", savedFood, "http://image.jpg");
        Category savedTargetCategory = categoryRepository.save(targetCategory);
        savedTargetCategory.generatePath();
        //when
        //then
        assertThatThrownBy(() -> categoryService.moveParent(savedTargetCategory.getId(), savedElectronics.getId(), false))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCdoe")
                .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
    }

    @Test
    @DisplayName("카테고리의 부모를 변경할때 부모 카테고리에 속한 상품이 존재하는 경우 예외를 던진다")
    void moveParent_product_in_parentCategory() {
        //given
        Category electronics = Category.create("전자기기", null, "http://image.jpg");
        Category savedElectronics = categoryRepository.save(electronics);
        savedElectronics.generatePath();
        Category food = Category.create("식품", null, "http://image.jpg");
        Category savedFood = categoryRepository.save(food);
        savedFood.generatePath();
        Category cellphone = Category.create("핸드폰", savedElectronics, "http://image.jpg");
        Category savedCellphone = categoryRepository.save(cellphone);
        savedCellphone.generatePath();

        // 식품 카테고리에 상품이 존재
        Product product = Product.create("식품", "상품 설명", savedFood);
        productRepository.save(product);
        //when
        //then
        assertThatThrownBy(() -> categoryService.moveParent(savedCellphone.getId(), savedFood.getId(), false))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.HAS_PRODUCT);
    }

    private CategoryTreeResponse findNodeByName(List<CategoryTreeResponse> nodes, String name) {
        return nodes.stream()
                .filter(node -> node.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError(name + " 카테고리를 찾을 수 없습니다."));
    }

    private Category exceedDepthDCategory(){
        Category category = Category.create("카테고리", null, "http://image.jpg");
        ReflectionTestUtils.setField(category, "depth", 5);
        return category;
    }
}
