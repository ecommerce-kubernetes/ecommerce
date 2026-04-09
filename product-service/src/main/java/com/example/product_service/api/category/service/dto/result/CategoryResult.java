package com.example.product_service.api.category.service.dto.result;

import com.example.product_service.api.category.domain.model.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CategoryResult {

    @Builder
    public record Detail(
            Long id,
            String name,
            Long parentId,
            Integer depth,
            String imagePath
    ) {
        public static Detail from(Category category) {
            return Detail.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .parentId(category.getParent() == null ? null : category.getParent().getId())
                    .depth(category.getDepth())
                    .imagePath(category.getImagePath())
                    .build();
        }
    }

    @Builder
    public record Navigation(
            Detail current,
            List<Detail> path,
            List<Detail> siblings,
            List<Detail> children
    ) { }

    @Getter
    public static class Tree {
        private final Long id;
        private final String name;
        private final Long parentId;
        private final Integer depth;
        private final String imagePath;
        private final List<Tree> children;

        @Builder
        public Tree(Long id, String name, Long parentId, Integer depth, String imagePath) {
            this.id = id;
            this.name = name;
            this.parentId = parentId;
            this.depth = depth;
            this.imagePath = imagePath;
            this.children = new ArrayList<>();
        }

        public void addChild(Tree child) {
            this.children.add(child);
        }

        public static Tree from(Category category) {
            return Tree.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .parentId(category.getParent() == null ? null : category.getParent().getId())
                    .depth(category.getDepth())
                    .imagePath(category.getImagePath())
                    .build();
        }
    }
}
