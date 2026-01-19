package com.example.product_service.api.category.domain.model;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.support.fixture.builder.CategoryTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryTest {

    @Nested
    @DisplayName("카테고리 생성")
    class Create {

        @Test
        @DisplayName("최상위 카테고리 생성")
        void create_root(){
            //given
            //when
            Category category = Category.create("루트", null, "http://root.jpg");
            //then
            assertThat(category)
                    .extracting(Category::getName, Category::getDepth, Category::getImageUrl)
                    .containsExactly("루트", 1, "http://root.jpg");
        }

        @Test
        @DisplayName("자식 카테고리 생성")
        void create_child(){
            //given
            Category parent = CategoryTestBuilder.aCategory().build();
            //when
            Category category = Category.create("자식", parent, "http://child.jpg");
            //then
            assertThat(category)
                    .extracting(Category::getName, Category::getDepth, Category::getImageUrl)
                    .containsExactly("자식", 2, "http://child.jpg");

            assertThat(parent.getChildren()).contains(category);
        }

        @Test
        @DisplayName("부모의 depth 가 최대인 경우 카테고리를 생성할 수 없다")
        void create_exceed_max_depth() {
            //given
            Category maxDepthCategory = CategoryTestBuilder.aCategory()
                    .withDepth(5).build();
            //when
            //then
            assertThatThrownBy(() -> Category.create("자식 카테고리", maxDepthCategory, "http://image.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }
    }

    @Nested
    @DisplayName("카테고리 경로 생성")
    class GeneratePath {

        @Test
        @DisplayName("카테고리 id 가 설정되지 않으면 카테고리 경로를 생성할 수 없다")
        void generatePath_id_null(){
            //given
            Category category = CategoryTestBuilder.aCategory().withId(null).build();
            //when
            //then
            assertThatThrownBy(category::generatePath)
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.CATEGORY_ID_IS_NULL);
        }

        @Test
        @DisplayName("최상위 카테고리의 경로를 생성한다")
        void generatePath_root(){
            //given
            Category category = CategoryTestBuilder.aCategory().withId(2L).build();
            //when
            category.generatePath();
            //then
            assertThat(category.getPath()).isEqualTo(String.valueOf(2L));
        }

        @Test
        @DisplayName("자식 카테고리의 경로를 생성한다")
        void generatePath_child(){
            //given
            Category parent = CategoryTestBuilder.aCategory()
                    .withId(2L).build();
            Category child = CategoryTestBuilder.aCategory()
                    .withParent(parent)
                    .withId(3L).build();
            //when
            child.generatePath();
            //then
            assertThat(child.getPath()).isEqualTo(2L + "/" + 3L);
        }
    }

    @Nested
    @DisplayName("카테고리 이름 수정")
    class UpdateName {

        @Test
        @DisplayName("카테고리 이름을 수정한다")
        void rename(){
            //given
            Category category = CategoryTestBuilder.aCategory().build();
            //when
            category.rename("전자기기");
            //then
            assertThat(category.getName()).isEqualTo("전자기기");
        }
    }

    @Nested
    @DisplayName("카테고리 이미지 수정")
    class UpdateImage {

        @Test
        @DisplayName("카테고리 이미지를 수정한다")
        void changeImage(){
            //given
            Category category = CategoryTestBuilder.aCategory().build();
            //when
            category.changeImage("http://newImage.jpg");
            //then
            assertThat(category.getImageUrl())
                    .isEqualTo("http://newImage.jpg");
        }
    }

    @Nested
    @DisplayName("카테고리 루트 여부")
    class IsRoot {

        @Test
        @DisplayName("카테고리가 루트 카테고리면 true를 반환한다")
        void isRoot_true(){
            //given
            Category category = CategoryTestBuilder.aCategory().build();
            //when
            boolean isRoot = category.isRoot();
            //then
            assertThat(isRoot).isTrue();
        }

        @Test
        @DisplayName("카테고리가 루트 카테고리가 아니면 false를 반환한다")
        void isRoot_false(){
            //given
            Category parent = CategoryTestBuilder.aCategory().build();
            Category category = CategoryTestBuilder
                    .aCategory().withParent(parent).build();
            //when
            boolean isRoot = category.isRoot();
            //then
            assertThat(isRoot).isFalse();
        }
    }

    @Nested
    @DisplayName("카테고리 경로 id 추출")
    class GetPathIds {

        @Test
        @DisplayName("루트 카테고리인 경우 자신의 아이디가 포함된 리스트를 반환한다")
        void getPathIds_root(){
            //given
            Category root = CategoryTestBuilder.aCategory()
                    .build();
            //when
            List<Long> pathIds = root.getPathIds();
            //then
            assertThat(pathIds)
                    .containsExactly(root.getId());
        }

        @Test
        @DisplayName("자식 카테고리인 경우 자신의 경로 아이디 리스트를 반환한다")
        void getPathIds_child(){
            //given
            Category parent = CategoryTestBuilder.aCategory()
                    .build();
            Category category = CategoryTestBuilder.aCategory().withParent(parent)
                    .build();
            //when
            List<Long> pathIds = category.getPathIds();
            //then
            assertThat(pathIds)
                    .containsExactly(parent.getId(), category.getId());
        }
    }

    @Nested
    @DisplayName("카테고리 부모 변경")
    class MoveParent {

        @Test
        @DisplayName("카테고리의 부모를 변경할때 부모의 depth 가 최대인 경우 부모를 변경할 수 없다")
        void moveParent_exceed_max_depth(){
            //given
            Category parent = CategoryTestBuilder.aCategory()
                    .withDepth(5).build();
            Category target = CategoryTestBuilder.aCategory()
                    .build();
            //when
            //then
            assertThatThrownBy(() -> target.moveParent(parent))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }
    }

    @Test
    @DisplayName("카테고리 생성시 parent 가 null 이면 depth가 1인 카테고리를 생성한다")
    void create_root(){
        //given
        //when
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //then
        assertThat(category)
                .extracting(Category::getName, Category::getParent, Category::getDepth, Category::getImageUrl)
                .containsExactly(
                        "카테고리", null, 1, "http://image.jpg"
                );
    }

    @Test
    @DisplayName("자식 카테고리 생성시 depth는 부모의 depth + 1 이다")
    void create_child(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        //when
        Category category = Category.create("자식", root, "http://image.jpg");
        //then
        assertThat(category)
                .extracting(Category::getName, Category::getParent, Category::getDepth, Category::getImageUrl)
                .containsExactly("자식", root, 2, "http://image.jpg");
    }

    @Test
    @DisplayName("카테고리 이름을 변경한다")
    void rename(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        category.rename("새 카테고리");
        //then
        assertThat(category.getName()).isEqualTo("새 카테고리");
    }

    @Test
    @DisplayName("변경할 이름이 유효하지 않으면 예외를 던진다")
    void rename_invalidName(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        //then
        assertThatThrownBy(() -> category.rename(" "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("카테고리 이미지를 변경한다")
    void changeImage(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        category.changeImage("http://newImage.jpg");
        //then
        assertThat(category.getImageUrl()).isEqualTo("http://newImage.jpg");
    }

    @Test
    @DisplayName("변경할 이미지가 유효하지 않으면 예외를 던진다")
    void changeImage_invalidImage(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        //then
        assertThatThrownBy(() -> category.changeImage("  "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("카테고리가 최상위 카테고리면 true를 반환한다")
    void isRoot_true(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        boolean isRoot = category.isRoot();
        //then
        assertThat(isRoot).isTrue();
    }

    @Test
    @DisplayName("카테고리가 최상위 카테고리가 아니면 fals를 반환한다")
    void isRoot_false(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        Category category = Category.create("자식", root, "http://image.jpg");
        //when
        boolean isRoot = category.isRoot();
        //then
        assertThat(isRoot).isFalse();
    }

    @Test
    @DisplayName("카테고리 경로를 생성한다")
    void generatePath(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        setId(root, 1L);
        //when
        root.generatePath();
        //then
        assertThat(root.getPath()).isEqualTo(String.valueOf(1L));
    }

    @Test
    @DisplayName("자식 카테고리 경로를 생성한다")
    void generatePath_child(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        setId(root, 1L);
        root.generatePath();
        Category child = Category.create("자식", root, "http://image.jpg");
        setId(child, 2L);
        //when
        child.generatePath();
        //then
        assertThat(child.getPath()).isEqualTo(root.getId() + "/" + child.getId());
    }

    @Test
    @DisplayName("조상 id를 조회한다")
    void getPathIds(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        setId(root, 1L);
        root.generatePath();
        Category child = Category.create("자식", root, "http://image.jpg");
        setId(child, 2L);
        child.generatePath();
        //when
        List<Long> ids = child.getPathIds();
        //then
        assertThat(ids).contains(1L, 2L);
    }

    @Test
    @DisplayName("카테고리 부모를 변경한다")
    void moveParent(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        setId(root, 1L);
        root.generatePath();
        Category child = Category.create("자식", root, "http://image.jpg");
        setId(child, 2L);
        child.generatePath();
        Category grandson = Category.create("손자", child, "http://image.jpg");
        setId(grandson, 3L);
        grandson.generatePath();

        Category newParent = Category.create("새 부모", null, "http://image.jpg");
        setId(newParent, 4L);
        newParent.generatePath();
        //when
        child.moveParent(newParent);
        //then
        assertThat(child.getParent()).isEqualTo(newParent);
        assertThat(child.getPath()).isEqualTo(newParent.getId() + "/" + child.getId());
        assertThat(grandson.getPath()).isEqualTo(newParent.getId() + "/" + child.getId() + "/" + grandson.getId());
    }

    @Test
    @DisplayName("최하위 카테고리인 경우 true를 반환한다")
    void isLeaf(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        //when
        boolean isLeaf = root.isLeaf();
        //then
        assertThat(isLeaf).isTrue();
    }

    private void setId(Category category, Long id) {
        ReflectionTestUtils.setField(category, "id", id);
    }
}
