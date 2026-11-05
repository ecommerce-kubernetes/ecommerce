package com.example.product_service.category.adapter.out.persistence;

import com.example.product_service.category.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryJpaRepositoryTest {

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("같은 부모 아래 동일한 이름이 존재하면 true를 반환한다")
    void existsByParentIdAndName_whenDuplicateExists_thenReturnsTrue() {
        //given
        Category parent = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        Category child = Category.createChild(2L, "노트북", "/category/laptop.jpg", parent);
        entityManager.persist(parent);
        entityManager.persist(child);
        entityManager.flush();
        //when
        boolean exists = categoryJpaRepository.existsByParentIdAndName(parent.getId(), "노트북");
        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("같은 부모 아래 동일한 이름이 없으면 false를 반환한다")
    void existsByParentIdAndName_whenNoDuplicate_thenReturnsFalse() {
        //given
        Category parent = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        entityManager.persist(parent);
        entityManager.flush();
        //when
        boolean exists = categoryJpaRepository.existsByParentIdAndName(parent.getId(), "노트북");
        //then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("최상위 카테고리끼리 동일한 이름이 존재하면 true를 반환한다")
    void existsByParentIdAndName_whenParentIdIsNull_thenChecksRootSiblings() {
        //given
        Category root = Category.createRoot(1L, "식품", "/category/food.jpg");
        entityManager.persist(root);
        entityManager.flush();
        //when
        boolean exists = categoryJpaRepository.existsByParentIdAndName(null, "식품");
        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("자기 자신을 제외하고 동일한 이름이 존재하면 true를 반환한다")
    void existsByParentIdAndNameAndIdNot_whenDuplicateExcludingSelf_thenReturnsTrue() {
        //given
        Category parent = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        Category child1 = Category.createChild(2L, "노트북", "/category/laptop.jpg", parent);
        Category child2 = Category.createChild(3L, "핸드폰", "/category/phone.jpg", parent);
        entityManager.persist(parent);
        entityManager.persist(child1);
        entityManager.persist(child2);
        entityManager.flush();
        //when
        boolean exists = categoryJpaRepository.existsByParentIdAndNameAndIdNot(parent.getId(), "노트북", child2.getId());
        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("자기 자신만 동일한 이름이면 false를 반환한다")
    void existsByParentIdAndNameAndIdNot_whenOnlySelfMatches_thenReturnsFalse() {
        //given
        Category parent = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        Category child = Category.createChild(2L, "노트북", "/category/laptop.jpg", parent);
        entityManager.persist(parent);
        entityManager.persist(child);
        entityManager.flush();
        //when
        boolean exists = categoryJpaRepository.existsByParentIdAndNameAndIdNot(parent.getId(), "노트북", child.getId());
        //then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("경로 접두사로 시작하는 모든 하위 카테고리를 조회한다")
    void findAllByPathStartingWith_returnsMatchingDescendants() {
        //given
        Category root = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        Category child = Category.createChild(2L, "노트북", "/category/laptop.jpg", root);
        Category grandchild = Category.createChild(3L, "게이밍 노트북", "/category/gaming.jpg", child);
        Category unrelated = Category.createRoot(4L, "식품", "/category/food.jpg");
        entityManager.persist(root);
        entityManager.persist(child);
        entityManager.persist(grandchild);
        entityManager.persist(unrelated);
        entityManager.flush();
        //when
        List<Category> result = categoryJpaRepository.findAllByPathStartingWith(root.getPath() + "/");
        //then
        assertThat(result).extracting(Category::getId)
                .containsExactlyInAnyOrder(child.getId(), grandchild.getId());
    }

    @Test
    @DisplayName("경로 접두사로 시작하는 카테고리가 없으면 빈 목록을 반환한다")
    void findAllByPathStartingWith_whenNoMatches_thenReturnsEmptyList() {
        //given
        Category root = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        entityManager.persist(root);
        entityManager.flush();
        //when
        List<Category> result = categoryJpaRepository.findAllByPathStartingWith("999/");
        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("부모가 없는 최상위 카테고리 목록을 조회한다")
    void findAllByParentIsNull_returnsRootCategories() {
        //given
        Category root1 = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        Category root2 = Category.createRoot(2L, "식품", "/category/food.jpg");
        Category child = Category.createChild(3L, "노트북", "/category/laptop.jpg", root1);
        entityManager.persist(root1);
        entityManager.persist(root2);
        entityManager.persist(child);
        entityManager.flush();
        //when
        List<Category> result = categoryJpaRepository.findAllByParentIsNull();
        //then
        assertThat(result).extracting(Category::getId)
                .containsExactlyInAnyOrder(root1.getId(), root2.getId());
    }

    @Test
    @DisplayName("최상위 카테고리가 없으면 빈 목록을 반환한다")
    void findAllByParentIsNull_whenNoRoots_thenReturnsEmptyList() {
        //given
        //when
        List<Category> result = categoryJpaRepository.findAllByParentIsNull();
        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특정 부모의 자식 카테고리 목록을 조회한다")
    void findAllByParentId_returnsChildren() {
        //given
        Category parent = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        Category child1 = Category.createChild(2L, "노트북", "/category/laptop.jpg", parent);
        Category child2 = Category.createChild(3L, "핸드폰", "/category/phone.jpg", parent);
        Category unrelated = Category.createRoot(4L, "식품", "/category/food.jpg");
        entityManager.persist(parent);
        entityManager.persist(child1);
        entityManager.persist(child2);
        entityManager.persist(unrelated);
        entityManager.flush();
        //when
        List<Category> result = categoryJpaRepository.findAllByParentId(parent.getId());
        //then
        assertThat(result).extracting(Category::getId)
                .containsExactlyInAnyOrder(child1.getId(), child2.getId());
    }

    @Test
    @DisplayName("자식 카테고리가 없으면 빈 목록을 반환한다")
    void findAllByParentId_whenNoChildren_thenReturnsEmptyList() {
        //given
        Category parent = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        entityManager.persist(parent);
        entityManager.flush();
        //when
        List<Category> result = categoryJpaRepository.findAllByParentId(parent.getId());
        //then
        assertThat(result).isEmpty();
    }
}
