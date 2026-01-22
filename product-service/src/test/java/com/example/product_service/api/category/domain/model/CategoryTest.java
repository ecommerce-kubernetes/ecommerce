package com.example.product_service.api.category.domain.model;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.support.fixture.builder.CategoryTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
        @DisplayName("최상위 카테고리로 변경한다")
        void moveParent_root() {
            //given
            Category oldParent = CategoryTestBuilder.aCategory()
                    .withId(2L)
                    .build();
            Category target = CategoryTestBuilder.aCategory()
                    .withId(3L)
                    .withParent(oldParent)
                    .build();
            Category targetChild = CategoryTestBuilder.aCategory()
                    .withId(4L)
                    .withParent(target)
                    .build();
            //when
            target.moveParent(null);
            //then
            assertThat(target.getParent()).isNull();
            assertThat(target.getDepth()).isEqualTo(1);
            assertThat(target.getPath()).isEqualTo(String.valueOf(3L));

            assertThat(targetChild)
                    .extracting(Category::getDepth, Category::getPath)
                    .containsExactly(2, 3L + "/" + 4L);
        }

        @Test
        @DisplayName("부모 카테고리를 변경한다")
        void moveParent_child() {
            //given
            Category oldParent = CategoryTestBuilder.aCategory().withId(2L).build();
            Category newParent = CategoryTestBuilder.aCategory().withId(3L).build();
            Category target = CategoryTestBuilder.aCategory().withId(4L).withParent(oldParent).build();
            Category targetChild = CategoryTestBuilder.aCategory().withId(5L).withParent(target).build();
            //when
            target.moveParent(newParent);
            //then
            assertThat(target.getParent()).isEqualTo(newParent);
            assertThat(target.getDepth()).isEqualTo(2);
            assertThat(target.getPath()).isEqualTo(3L + "/" + 4L);

            assertThat(targetChild)
                    .extracting(Category::getDepth, Category::getPath)
                    .containsExactly(3, 3L + "/" + 4L + "/" + 5L);
        }
        
        @Test
        @DisplayName("자기 자신을 부모로 변경할 수 없다")
        void moveParent_myself() {
            //given
            Category category = CategoryTestBuilder.aCategory().build();
            //when
            //then
            assertThatThrownBy(() -> category.moveParent(category))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.CANNOT_MOVE_TO_SELF);
        }

        @Test
        @DisplayName("자신의 자손을 부모로 변경할 수 없다")
        void moveParent_descendant() {
            //given
            Category target = CategoryTestBuilder.aCategory().withId(1L).build();
            Category child = CategoryTestBuilder.aCategory().withId(2L).withParent(target).build();
            Category grandSon = CategoryTestBuilder.aCategory().withId(3L).withParent(child).build();
            //when
            //then
            assertThatThrownBy(() -> target.moveParent(grandSon))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.CANNOT_MOVE_TO_DESCENDANT);
        }

        @Test
        @DisplayName("카테고리의 부모를 변경할때 부모의 depth 가 최대인 경우 부모를 변경할 수 없다")
        void moveParent_exceed_max_depth(){
            //given
            Category parent = CategoryTestBuilder.aCategory().withId(2L)
                    .withDepth(5).build();
            Category target = CategoryTestBuilder.aCategory().withId(3L)
                    .build();
            //when
            //then
            assertThatThrownBy(() -> target.moveParent(parent))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }
    }

    @Nested
    @DisplayName("최하위 카테고리 여부")
    class IsLeaf {

        @Test
        @DisplayName("최하위 카테고리라면 true를 반환한다")
        void isLeaf_true() {
            //given
            Category parent = CategoryTestBuilder.aCategory().build();
            Category child = CategoryTestBuilder.aCategory().withParent(parent).build();
            //when
            boolean isLeaf = child.isLeaf();
            //then
            assertThat(isLeaf).isTrue();
        }

        @Test
        @DisplayName("최하위 카테고리라면 true를 반환한다")
        void isLeaf_false() {
            //given
            Category parent = CategoryTestBuilder.aCategory().build();
            Category child = CategoryTestBuilder.aCategory().withParent(parent).build();
            //when
            boolean isLeaf = parent.isLeaf();
            //then
            assertThat(isLeaf).isFalse();
        }
    }
}
