package com.example.product_service.api.category.domain.repository;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class CategoryRepositoryTest extends ExcludeInfraTest {

    @Autowired
    private CategoryRepository repository;

    @Test
    @DisplayName("최상위 카테고리를 조회한다")
    void findByParentIsNull(){
        //given
        Category root1 = Category.create("1번", null, "http://image.jpg");
        Category root2 = Category.create("2번", null, "http://image.jpg");
        Category root3 = Category.create("3번", null, "http://image.jpg");
        Category child1 = Category.create("자식", root3, "http://image.jpg");
        repository.saveAll(List.of(root1, root2, root3, child1));
        //when
        List<Category> roots = repository.findByParentIsNull();
        //then
        assertThat(roots)
                .extracting(Category::getId)
                .containsExactlyInAnyOrder(root1.getId(), root2.getId(), root3.getId());
    }

    @Test
    @DisplayName("depth가 낮은 순으로 카테고리를 조회한다")
    void findByInOrderDepth(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        Category child = Category.create("자식", root, "http://image.jpg");
        Category grandson = Category.create("자식", child, "http://image.jpg");
        repository.saveAll(List.of(root, child, grandson));
        //when
        List<Category> results = repository.findByInOrderDepth(List.of(root.getId(), child.getId()));
        //then
        assertThat(results)
                .extracting(Category::getId)
                .containsExactly(root.getId(), child.getId());
    }

    @Test
    @DisplayName("특정 부모의 자식 카테고리를 조회한다")
    void findByParentId(){
        //given
        Category root = Category.create("1번", null, "http://image.jpg");
        Category child = Category.create("자식", root, "http://image.jpg");
        Category sibling = Category.create("형제", root, "http://image.jpg");
        repository.saveAll(List.of(root, child, sibling));
        //when
        List<Category> results = repository.findByParentId(root.getId());
        //then
        assertThat(results)
                .extracting(Category::getId)
                .containsExactlyInAnyOrder(child.getId(), sibling.getId());
    }

    @Test
    @DisplayName("형제중 동일한 이름이 있다면 true 를 반환한다")
    void existsDuplicateName(){
        //given
        Category root = Category.create("1번", null, "http://image.jpg");
        Category child = Category.create("자식", root, "http://image.jpg");
        Category sibling = Category.create("형제", root, "http://image.jpg");
        repository.saveAll(List.of(root, child, sibling));
        //when
        boolean isDuplicate = repository.existsDuplicateName(root.getId(), "자식");
        //then
        assertThat(isDuplicate).isTrue();
    }
}
