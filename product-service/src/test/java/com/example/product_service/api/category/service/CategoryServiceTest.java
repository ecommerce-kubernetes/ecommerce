package com.example.product_service.api.category.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.support.ExcludeInfraTest;
import com.example.product_service.entity.Product;
import com.example.product_service.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class CategoryServiceTest extends ExcludeInfraTest {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    private Category setupCategory(String name, Category parent) {
        Category category = Category.create(name, parent, "http://test.jpg");
        categoryRepository.save(category);
        category.generatePath();
        return category;
    }

    private Product setupProduct(String name, Category category) {
        Product product = Product.create(name, "설명", category);
        return productRepository.save(product);
    }

    private Category setupMaxDepthCategory() {
        Category root = setupCategory("루트", null);
        Category depth2 = setupCategory("depth2", root);
        Category depth3 = setupCategory("depth3", depth2);
        Category depth4 = setupCategory("depth4", depth3);
        return setupCategory("depth5", depth4);
    }

    @Nested
    @DisplayName("카테고리 생성시")
    class Create {

        @Test
        @DisplayName("최상위 카테고리를 생성한다")
        void save_root(){
            //when
            CategoryResponse result = categoryService.saveCategory("가전", null, "http://img.jpg");
            //then
            assertThat(result)
                    .extracting(CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth)
                    .containsExactly("가전", null, 1);

            Category saved = categoryRepository.findById(result.getId()).orElseThrow();
            assertThat(saved.getPath()).isEqualTo(String.valueOf(saved.getId()));
        }

        @Test
        @DisplayName("")
        void save_child(){
            //given
            Category parent = setupCategory("가전", null);
            //when
            CategoryResponse result = categoryService.saveCategory("노트북", parent.getId(), "http://image.jpg");
            //then
            assertThat(result)
                    .extracting(CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth)
                    .containsExactly("노트북", parent.getId(), 2);

            Category saved = categoryRepository.findById(result.getId()).orElseThrow();
            assertThat(saved.getPath()).isEqualTo(parent.getId() + "/" + saved.getId());
        }

        @Test
        @DisplayName("카테고리를 생성할때 부모 카테고리를 찾을 수 없으면 예외를 던진다")
        void save_when_notFound_parent(){
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
        void save_when_exceed_max_depth(){
            //given
            Category parent = setupMaxDepthCategory();
            //when
            //then
            assertThatThrownBy(() -> categoryService.saveCategory("자식", parent.getId(), "http://image.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }

        @Test
        @DisplayName("형제중 같은 이름이 존재하면 예외를 던진다[최상위 카테고리 생성]")
        void save_when_duplicate_name_root(){
            //given
            setupCategory("가전", null);
            //when
            //then
            assertThatThrownBy(() -> categoryService.saveCategory("가전", null, "http://image.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
        }

        @Test
        @DisplayName("형제중 같은 이름이 존재하면 예외를 던진다[자식 카테고리 생성]")
        void save_when_duplicate_name_child(){
            //given
            Category parent = setupCategory("가전", null);
            setupCategory("TV", parent);
            //when
            //then
            assertThatThrownBy(() -> categoryService.saveCategory("TV", parent.getId(), "http://image.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
        }

        @Test
        @DisplayName("부모 카테고리에 상품이 존재하면 예외를 던진다")
        void save_parent_has_product(){
            //given
            Category parent = setupCategory("부모", null);
            setupProduct("상품", parent);
            //when
            //then
            assertThatThrownBy(() -> categoryService.saveCategory("자식", parent.getId(), "http://image.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.HAS_PRODUCT);
        }
    }

    @Nested
    @DisplayName("카테고리 조회")
    class Read {

        @Test
        @DisplayName("카테고리 단건을 조회한다")
        void getCategory(){
            //given
            Category category = setupCategory("카테고리", null);
            //when
            CategoryResponse result = categoryService.getCategory(category.getId());
            //then
            assertThat(result)
                    .extracting(CategoryResponse::getId, CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth)
                    .containsExactly(category.getId(), "카테고리", null, 1);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리를 조회하면 예외를 던진다")
        void getCategory_notFound(){
            //given
            //when
            //then
            assertThatThrownBy(() -> categoryService.getCategory(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("전체 트리 구조를 조회하면 정렬된 트리구조를 반환한다")
        void getTree(){
            //given
            Category root1 = setupCategory("전자", null);
            Category root2 = setupCategory("식품", null);

            Category child1 = setupCategory("노트북", root1);
            Category child2 = setupCategory("냉장고", root1);
            //when
            List<CategoryTreeResponse> result = categoryService.getTree();
            //then
            assertThat(result).extracting(CategoryTreeResponse::getName, CategoryTreeResponse::getDepth)
                    .containsExactly(
                            tuple("전자", 1),
                            tuple("식품", 1)
                    );

            CategoryTreeResponse electronics = result.get(0);
            assertThat(electronics.getChildren()).extracting(CategoryTreeResponse::getName, CategoryTreeResponse::getDepth)
                    .containsExactly(
                            tuple("노트북", 2),
                            tuple("냉장고", 2)
                    );
        }

        @Test
        @DisplayName("카테고리 네비게이션을 조회한다")
        void getNavigation(){
            //given
            Category root = setupCategory("전자", null);
            Category depth2 = setupCategory("컴퓨터", root);
            Category target = setupCategory("노트북", depth2);
            Category siblings = setupCategory("데스크탑", depth2);
            Category child = setupCategory("삼성", target);
            //when
            CategoryNavigationResponse result = categoryService.getNavigation(target.getId());
            //then
            assertThat(result.getCurrent())
                    .extracting(CategoryResponse::getName, CategoryResponse::getDepth)
                    .containsExactly("노트북", 3);

            assertThat(result.getAncestors())
                    .extracting(CategoryResponse::getName, CategoryResponse::getDepth)
                    .containsExactly(
                            tuple("전자", 1),
                            tuple("컴퓨터", 2),
                            tuple("노트북", 3)
                    );

            assertThat(result.getSiblings())
                    .extracting(CategoryResponse::getName, CategoryResponse::getDepth)
                    .containsExactly(
                            tuple("노트북", 3),
                            tuple("데스크탑", 3)
                    );

            assertThat(result.getChildren())
                    .extracting(CategoryResponse::getName, CategoryResponse::getDepth)
                    .containsExactly(
                            tuple("삼성", 4)
                    );
        }
    }

    @Nested
    @DisplayName("카테고리 수정")
    class Update {

        @Test
        @DisplayName("카테고리 정보를 수정한다")
        void update(){
            //given
            Category category = setupCategory("기존", null);
            //when
            CategoryResponse result = categoryService.updateCategory(category.getId(), "변경", "http://newimage.jpg");
            //then
            assertThat(result)
                    .extracting(CategoryResponse::getName, CategoryResponse::getImageUrl)
                    .containsExactly("변경", "http://newimage.jpg");
        }

        @Test
        @DisplayName("변경할 카테고리를 찾을 수 없으면 예외를 던진다")
        void update_notFound(){
            //given
            //when
            //then
            assertThatThrownBy(() -> categoryService.updateCategory(999L, "변경", "http://newImage.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("변경하는 이름이 형제중에 존재하면 예외를 던진다[최상위 카테고리]")
        void update_duplicateName_root(){
            //given
            Category existName = setupCategory("동일한 이름", null);
            Category target = setupCategory("타깃 카테고리", null);
            //when
            //then
            assertThatThrownBy(() -> categoryService.updateCategory(target.getId(), "동일한 이름", "http://image.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
        }

        @Test
        @DisplayName("변경하는 이름이 형제중에 존재하면 예외를 던진다[자식 카테고리]")
        void update_duplicateName_child(){
            //given
            Category root = setupCategory("루트", null);
            Category existName = setupCategory("동일한 이름", root);
            Category target = setupCategory("타깃 카테고리", root);
            //when
            //then
            assertThatThrownBy(() -> categoryService.updateCategory(target.getId(), "동일한 이름", "http://image.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
        }
    }

    @Nested
    @DisplayName("카테고리 이동")
    class Move {

        @Test
        @DisplayName("카테고리를 다른 부모 밑으로 이동한다")
        void move_child(){
            //given
            Category root1 = setupCategory("전자", null);
            Category target = setupCategory("컴퓨터", root1);
            Category child = setupCategory("노트북", target);

            Category root2 = setupCategory("식품", null);
            //when
            CategoryResponse result = categoryService.moveParent(target.getId(), root2.getId(), false);
            //then
            assertThat(result)
                    .extracting(CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth)
                    .containsExactly("컴퓨터", root2.getId(), 2);

            Category updatedChild = categoryRepository.findById(child.getId()).orElseThrow();
            assertThat(updatedChild.getPath())
                    .isEqualTo(root2.getId() + "/" + target.getId() + "/" + child.getId());
        }

        @Test
        @DisplayName("카테고리를 최상위 카테고리로 변경한다")
        void move_root(){
            //given
            Category root = setupCategory("전자", null);
            Category target = setupCategory("노트북", root);
            //when
            CategoryResponse result = categoryService.moveParent(target.getId(), null, true);
            //then
            assertThat(result)
                    .extracting(CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getDepth)
                    .containsExactly("노트북", null, 1);

            Category find = categoryRepository.findById(target.getId()).orElseThrow();
            assertThat(find.getPath()).isEqualTo(String.valueOf(find.getId()));
        }

        @Test
        @DisplayName("카테고리를 루트로 변경할때 동일한 이름이 있는 경우 예외를 던진다")
        void move_duplicateName_root(){
            //given
            setupCategory("동일한 이름", null);

            Category root = setupCategory("식품", null);
            Category target = setupCategory("동일한 이름", root);
            //when
            //then
            assertThatThrownBy(() -> categoryService.moveParent(target.getId(), null, true))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
        }

        @Test
        @DisplayName("카테고리를 이동할때 이동할 부모 카테고리의 자식 중 동일한 이름의 카테고리가 있는 경우 예외를 던진다")
        void move_duplicateName_siblings(){
            //given
            Category root1 = setupCategory("가전", null);
            setupCategory("동일한 이름", root1);

            Category root2 = setupCategory("식품", null);
            Category target = setupCategory("동일한 이름", root2);
            //when
            //then
            assertThatThrownBy(() -> categoryService.moveParent(target.getId(), root1.getId(), false))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.DUPLICATE_NAME);
        }

        @Test
        @DisplayName("카테고리를 이동할때 이동할 부모가 이미 최대 깊이라면 예외를 던진다")
        void move_exceed_max_depth(){
            //given
            Category category = setupMaxDepthCategory();
            Category target = setupCategory("카테고리", null);
            //when
            //then
            assertThatThrownBy(() -> categoryService.moveParent(target.getId(), category.getId(), false))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }

        @Test
        @DisplayName("자기자신을 부모로 설정하려고 하면 예외를 던진다")
        void move_parent_change_itself(){
            //given
            Category category = setupCategory("자기자신", null);
            //when
            //then
            assertThatThrownBy(() -> categoryService.moveParent(category.getId(), category.getId(), false))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.INVALID_HIERARCHY);
        }

        @Test
        @DisplayName("자신의 자식을 부모로 설정하려 하면 예외를 던진다")
        void move_parent_change_own_child(){
            //given
            Category root = setupCategory("전자기기", null);
            Category child = setupCategory("노트북", root);
            //when
            //then
            assertThatThrownBy(() -> categoryService.moveParent(root.getId(), child.getId(), false))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.INVALID_HIERARCHY);
        }

        @Test
        @DisplayName("변경하려는 부모 카테고리에 속한 상품이 있는 경우 예외를 던진다")
        void move_product_in_parent(){
            //given
            Category root1 = setupCategory("전자기기", null);
            Category target = setupCategory("노트북", root1);

            Category root2 = setupCategory("식품", null);
            setupProduct("상품", root2);
            //when
            //then
            assertThatThrownBy(() -> categoryService.moveParent(target.getId(), root2.getId(), false))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.HAS_PRODUCT);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class Delete {

        @Test
        @DisplayName("카테고리를 삭제한다")
        void delete(){
            //given
            Category root = setupCategory("전자기기", null);
            Category target = setupCategory("노트북", root);
            //when
            categoryService.deleteCategory(target.getId());
            //then
            Optional<Category> find = categoryRepository.findById(target.getId());
            assertThat(find).isEmpty();
        }

        @Test
        @DisplayName("삭제하려는 카테고리가 자식을 가지고 있는 경우 예외를 던진다")
        void delete_have_child(){
            //given
            Category target = setupCategory("전자기기", null);
            setupCategory("노트북", target);
            //when
            //then
            assertThatThrownBy(() -> categoryService.deleteCategory(target.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.HAS_CHILD);
        }

        @Test
        @DisplayName("삭제하려는 카테고리에 속한 상품이 존재하는 경우 예외를 던진다")
        void delete_product_in_category(){
            //given
            Category target = setupCategory("전자기기", null);
            setupProduct("상품", target);
            //when
            //then
            assertThatThrownBy(() -> categoryService.deleteCategory(target.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.HAS_PRODUCT);
        }
    }
}
