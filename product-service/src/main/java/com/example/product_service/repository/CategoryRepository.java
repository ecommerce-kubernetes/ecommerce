package com.example.product_service.repository;

import com.example.product_service.entity.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Categories, Long> {

    boolean existsByName(String name);
    List<Categories> findByParentIsNull();

    @Query("SELECT c FROM Categories c WHERE c.parent.id = :parentId")
    List<Categories> findChildById(@Param("parentId") Long parentId);

    @EntityGraph(attributePaths = "parent")
    @Query("SELECT c FROM Categories c WHERE c.id = :id")
    Optional<Categories> findByIdWithParent(@Param("id") Long id);



    /*
    * MySQL CTE(Common Table Expression) 을 사용해 Categories 와 그 자손 Categories 의 Id를 모두 추출
    * WITH RECURSIVE 는 CTE 을 선언할때 사용하는 키워드
    * CTE 임시테이블을 생성해서 그 안에서 자신을 재귀 호출해 데이터를 누적할 수 있도록 함
    *
    * 초기쿼리 SELECT id, parent_id FROM categories WHERE id = :rootId 로 rootId인 루트 카테고리를 선택
    * 재귀쿼리 SELECT c.id, c.parent_id FROM categories c JOIN category_tree ct ON c.parent_id = ct.id
    * 로 category_tree 에 추가된 category 의 id가 parent_id 인 category 를 찾아 category_tree 에 추가
    *
    * 마지막으로 모든 categoryId가 저장된 category_tree 테이블을 조회
    * */
    @Query(value = """
            WITH RECURSIVE category_tree AS (
                SELECT id, parent_id
                FROM categories
                WHERE id = :rootId
                UNION ALL
                SELECT c.id, c.parent_id
                FROM categories c
                JOIN category_tree ct
                ON c.parent_id = ct.id
            )
            SELECT id FROM category_tree
            """,
    nativeQuery = true)
    List<Long> findAllCategoryTreeIds(@Param("rootId") Long rootId);
}
