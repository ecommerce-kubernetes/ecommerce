package com.example.product_service.category.application.service.dto.result;

import com.example.product_service.category.domain.Category;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public record TreeCategoriesResult(
        List<TreeCategoryResult> categories
) {

    @Builder
    public record TreeCategoryResult(
            Long id,

            String name,

            int depth,

            String imagePath,

            boolean isLeaf,

            List<TreeCategoryResult> children
    ) {
        private static TreeCategoryResult buildNode(Category current, Map<Long, List<Category>> childrenMap) {
            List<Category> children = childrenMap.getOrDefault(current.getId(), Collections.emptyList());

            List<TreeCategoryResult> childNodes = children.stream()
                    .map(child -> buildNode(child, childrenMap))
                    .toList();

            return TreeCategoryResult.builder()
                    .id(current.getId())
                    .name(current.getName())
                    .depth(current.getDepth())
                    .imagePath(current.getImagePath())
                    .isLeaf(children.isEmpty())
                    .children(childNodes)
                    .build();
        }
    }

    public static TreeCategoriesResult from(List<Category> categories) {
        Map<Long, List<Category>> childrenMap = categories.stream()
                .filter(c -> !c.isRoot())
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<TreeCategoryResult> treeNodes = categories.stream()
                .filter(Category::isRoot)
                .map(root -> TreeCategoryResult.buildNode(root, childrenMap))
                .toList();

        return TreeCategoriesResult.builder()
                .categories(treeNodes)
                .build();
    }
}
